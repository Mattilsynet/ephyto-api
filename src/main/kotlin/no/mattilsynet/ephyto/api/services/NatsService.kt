package no.mattilsynet.ephyto.api.services

import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeFailedDto
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeMetadataDto
import no.mattilsynet.ephyto.api.nats.jetstream.subjects.JetStreamSubjectBuilder
import no.mattilsynet.virtualnats.virtualnatscore.VirtualNats
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NatsService(nats: VirtualNats) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val jetStream = nats.jetStream()


    fun publishImportEnvelopeMetadata(envelopeMetadataDto: EnvelopeMetadataDto) {
        jetStream.publish(
            subject = JetStreamSubjectBuilder.ephytoImportEnvelopesV1(),
            body = envelopeMetadataDto.toByteArray(),
        )
        logger.info(
            "Envelope ${envelopeMetadataDto.envelopeHeader.hubDeliveryNumber} publisert til" +
                    " ${JetStreamSubjectBuilder.ephytoImportEnvelopesV1()}"
        )
    }

    fun publishImportEnvelopeFailed(envelopeFailedDto: EnvelopeFailedDto) {
        jetStream.publish(
            subject = JetStreamSubjectBuilder.ephytoImportEnvelopesFailedV1(),
            body = envelopeFailedDto.toByteArray(),
        )

        logger.info(
            "Envelope ${envelopeFailedDto.hubDeliveryNumber} publisert til" +
                    " ${JetStreamSubjectBuilder.ephytoImportEnvelopesFailedV1()}"
        )
    }
}
