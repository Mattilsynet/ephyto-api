package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.HUBTrackingInfo
import com.google.protobuf.Timestamp
import no.mattilsynet.ephyto.api.domain.Valideringsresultat
import no.mattilsynet.ephyto.api.imports.envelope.v1.ArrayOfEnvelopeForwardings
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeDto
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeFailedDto
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeForwarding
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeHeaderDto
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeMetadataDto
import no.mattilsynet.ephyto.api.storage.blobstoragemetadata.v1.BlobStorageMetadata
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class EnvelopeService(
    private val gcpStorageService: GcpStorageService,
    @Value("\${storage.bucket.name}") private val importEnvelopesBucketName: String,
    private val natsService: NatsService,
    @Value("\${spring.profiles.active}") private val profile: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun haandterNyEnvelope(envelope: Envelope, valideringsresultat: Valideringsresultat): Valideringsresultat
    {
        lagEnvelopeDto(
            blobStorageMetadata = lagBlobStorageMetadata(envelope.nppoCertificateNumber),
            envelope = envelope,
            hubTrackingInfo = valideringsresultat.hubTrackingInfo,
        ).also { envelopeDto ->
            val blob = gcpStorageService.lastOppEn(
                bucketName = envelopeDto.envelopeMetadata.blobStorageMetadata.dataStorageName,
                contentBytes = envelopeDto.toByteArray(),
                dataUrl = envelopeDto.envelopeMetadata.blobStorageMetadata.dataUrl,
                hubLeveringNummer = envelopeDto.envelopeMetadata.envelopeHeader.hubDeliveryNumber,
            )

            when {
                // Skal ikke sende tilbakemelding når det var problemer med lagring i gcp
                blob == null -> valideringsresultat.also { it.validatedOk = null }

                (valideringsresultat.hubTrackingInfo != HUBTrackingInfo.DELIVERED &&
                valideringsresultat.hubTrackingInfo != HUBTrackingInfo.DELIVERED_WITH_WARNINGS)-> {
                    natsService.publishImportEnvelopeFailed(
                        lagEnvelopeFailedDto(
                            blobStorageMetadata = envelopeDto.envelopeMetadata.blobStorageMetadata,
                            envelope = envelope,
                            hubTrackingInfo = valideringsresultat.hubTrackingInfo,
                        )
                    )
                }

                else -> natsService.publishImportEnvelopeMetadata(envelopeDto.envelopeMetadata)
            }

            logger.info("Envelope ${envelope.hubDeliveryNumber} er ferdig håndtert.")
            return valideringsresultat
        }
    }

    private fun lagBlobStorageMetadata(nppoCertificateNumber: String): BlobStorageMetadata =
        BlobStorageMetadata.newBuilder()
            .setStorageProviderType(BlobStorageMetadata.StorageProviderType.GCP)
            .setDatastoreType(BlobStorageMetadata.StorageProviderDatastoreType.GCP_BUCKET)
            .setDataStorageName(importEnvelopesBucketName)
            .setDataUrl("$profile/$nppoCertificateNumber--${UUID.randomUUID()}")
            .build()

    private fun lagEnvelopeFailedDto(
        blobStorageMetadata: BlobStorageMetadata,
        envelope: Envelope,
        hubTrackingInfo: HUBTrackingInfo,
    ) =
        EnvelopeFailedDto.newBuilder()
            .setHubTrackingInfo(hubTrackingInfo.value())
            .setBlobStorageMetadata(blobStorageMetadata)
            .setNppoCertificateNumber(envelope.nppoCertificateNumber)
            .setHubDeliveryNumber(envelope.hubDeliveryNumber)
            .build()

    private fun lagEnvelopeDto(
        blobStorageMetadata: BlobStorageMetadata,
        envelope: Envelope,
        hubTrackingInfo: HUBTrackingInfo,
    ): EnvelopeDto =
        EnvelopeDto.newBuilder()
            .setEnvelopeMetadata(
                lagEnvelopeMetadataDto(
                    blobStorageMetadata = blobStorageMetadata,
                    envelope = envelope,
                    hubTrackingInfo = hubTrackingInfo,
                )
            )
            .setContent(envelope.content)
            .build()

    private fun lagEnvelopeMetadataDto(
        blobStorageMetadata: BlobStorageMetadata,
        envelope: Envelope,
        hubTrackingInfo: HUBTrackingInfo,
    ): EnvelopeMetadataDto =
        EnvelopeMetadataDto.newBuilder()
            .setBlobStorageMetadata(blobStorageMetadata)
            .setEnvelopeHeader(lagEnvelopeHeaderDto(envelope = envelope, hubTrackingInfo = hubTrackingInfo))
            .setReceivedAt(
                Instant.now().let { instant ->
                    Timestamp.newBuilder()
                        .setSeconds(instant.epochSecond)
                        .setNanos(instant.nano)
                        .build()
                }
            )
            .build()

    private fun lagEnvelopeHeaderDto(
        envelope: Envelope,
        hubTrackingInfo: HUBTrackingInfo,
    ): EnvelopeHeaderDto =
        with(envelope) {
            EnvelopeHeaderDto.newBuilder()
                .setArrayOfEnvelopeForwardings(
                    ArrayOfEnvelopeForwardings.newBuilder()
                        .addAllEnvelopeForwardings(forwardings?.envelopeForwarding?.mapNotNull {
                                envelopeForwarding ->
                            EnvelopeForwarding.newBuilder()
                                .setCode(envelopeForwarding.code)
                                .setHubDeliveryNumber(envelopeForwarding.hubDeliveryNumber)
                                .build()
                        } ?: emptyList())
                )
                .setCertificateStatus(certificateStatus)
                .setCertificateType(certificateType)
                .setFrom(from)
                .setHubDeliveryErrorMessage(hubDeliveryErrorMessage ?: "")
                .setHubDeliveryNumber(hubDeliveryNumber)
                .setHubTrackingInfo(hubTrackingInfo.value())
                .setNppoCertificateNumber(nppoCertificateNumber)
                .setTo(to)
                .build()
        }
}
