package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.hub.Envelope
import no.mattilsynet.ephyto.api.clients.EphytoDeliveryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class EphytoGetSingleImportEnvelopeService(
    private val ephytoDeliveryService: EphytoDeliveryService
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Retryable(
        backoff = Backoff(
            delay = 500,
            multiplier = 2.0,
        ),
        maxAttempts = 3,
        value = [Exception::class],
    )
    fun getSingleImportEnvelope(hubLeveringNummer: String): Envelope =
            ephytoDeliveryService.getClientConnection().pullSingleImportEnvelope(hubLeveringNummer)

    @Suppress("Unused")
    @Recover
    fun recover(ex: Exception, hubLeveringNummer: String): Envelope {
        logger.warn("Kunne ikke hente sertifikat med hubLeveringNummer $hubLeveringNummer etter retries, " +
                "Exception: ${ex.message}", ex)
        throw ex
    }
}
