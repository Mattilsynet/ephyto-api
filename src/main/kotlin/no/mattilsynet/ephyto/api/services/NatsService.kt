package no.mattilsynet.ephyto.api.services

import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeFailedDto
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeMetadataDto
import no.mattilsynet.ephyto.api.nats.jetstream.subjects.JetStreamSubjectBuilder
import no.mattilsynet.fisk.libs.reactivenats.ReactiveNats
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NatsService(
    private val reactiveNats: ReactiveNats,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun publishImportEnvelopeMetadata(envelopeMetadataDto: EnvelopeMetadataDto) {
        reactiveNats.jetStream().publish(
            subject = JetStreamSubjectBuilder.ephytoImportEnvelopesV1(),
            body = envelopeMetadataDto.toByteArray(),
        ).subscribe()

        logger.info("Envelope ${envelopeMetadataDto.envelopeHeader.hubDeliveryNumber} publisert til" +
                " ${JetStreamSubjectBuilder.ephytoImportEnvelopesV1()}")
    }

    fun publishImportEnvelopeFailed(envelopeFailedDto: EnvelopeFailedDto) {
        reactiveNats.jetStream().publish(
            subject = JetStreamSubjectBuilder.ephytoImportEnvelopesFailedV1(),
            body = envelopeFailedDto.toByteArray(),
        ).subscribe()

        logger.info("Envelope ${envelopeFailedDto.hubDeliveryNumber} publisert til" +
                " ${JetStreamSubjectBuilder.ephytoImportEnvelopesFailedV1()}")
    }
}
