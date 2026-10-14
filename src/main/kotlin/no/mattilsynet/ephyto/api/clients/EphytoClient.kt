package no.mattilsynet.ephyto.api.clients

import _int.ippc.ephyto.ValidationResult
import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.EnvelopeHeader
import no.mattilsynet.ephyto.api.domain.Valideringsresultat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.resilience.annotation.Retryable
import org.springframework.retry.annotation.Recover
import org.springframework.stereotype.Component

@Component
abstract class EphytoClient {

    @Autowired
    protected lateinit var ephytoDeliveryService: EphytoDeliveryService

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    abstract fun acknowledgeSuccessfulEnvelope(valideringsresultat: Valideringsresultat): Boolean

    abstract fun acknowledgeFailedEnvelope(valideringsresultat: Valideringsresultat): Boolean

    fun acknowledgeEnvelope(valideringsresultat: Valideringsresultat) {
        when (valideringsresultat.validatedOk) {
            true -> acknowledgeSuccessfulEnvelope(valideringsresultat)
            false -> acknowledgeFailedEnvelope(valideringsresultat)
            else -> Unit
        }
    }

    fun getImportEnvelopeHeaders(): List<EnvelopeHeader>? =
        runCatching {
            ephytoDeliveryService.getClientConnection()
                .getImportEnvelopeHeaders("")?.envelopeHeader
        }.onFailure {
            logger.warn("Kunne ikke hente nye sertifikat headere fra ephyto. Exception: ${it.message}",
                it)
        }.getOrNull()

    fun validatePhytoXml(phytoXml: String): List<ValidationResult> =
        runCatching {
            ephytoDeliveryService.getClientConnection().validatePhytoXML(phytoXml).toList()
        }.getOrElse {
            logger.warn(it.message, it)
            emptyList()
        }

    @Retryable(
        delay = 500,
        maxRetries = 3,
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
