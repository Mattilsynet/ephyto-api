package no.mattilsynet.ephyto.api.clients

import _int.ippc.ephyto.ValidationResult
import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.EnvelopeHeader
import no.mattilsynet.ephyto.api.domain.Valideringsresultat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
abstract class EphytoClient {

    @Autowired
    protected lateinit var ephytoDeliveryService: EphytoDeliveryService

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    abstract fun acknowledgeSuccessfulEnvelope(hubLeveringNummer: String): Boolean

    abstract fun acknowledgeFailedEnvelope(hubLeveringNummer: String, errorMessage: String): Boolean

    fun acknowledgeEnvelope(valideringsresultat: Valideringsresultat) {
        with(valideringsresultat) {
            when(validatedOk) {
                true -> acknowledgeSuccessfulEnvelope(hubLeveringNummer)
                else -> acknowledgeFailedEnvelope(hubLeveringNummer, errorMessage!!)
            }
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

    fun getSingleImportEnvelopeOrNull(hubLeveringNummer: String): Envelope? =
        runCatching {
            ephytoDeliveryService.getClientConnection().pullSingleImportEnvelope(hubLeveringNummer)

        }.onFailure {
            logger.warn("Kunne ikke hente sertifikat med hubLeveringNummer $hubLeveringNummer, " +
                    "Exception: ${it.message}",
                it)
        }.getOrNull()
}
