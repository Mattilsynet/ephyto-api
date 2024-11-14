package no.mattilsynet.ephyto.api.clients

import org.springframework.stereotype.Component

@Component
class EphytoClientAcknowledge : EphytoClient() {

    override fun acknowledgeSuccessfulEnvelope(hubLeveringNummer: String): Boolean =
        runCatching {
            ephytoDeliveryService
                .getClientConnection()
                .acknowledgeEnvelopeReceipt(hubLeveringNummer)
            logger.info("Sender successful acknowledgment for hubLeveringNummer $hubLeveringNummer")

        }.onFailure {
            logger.warn("Kunne ikke bekrefte ok mottak av sertifikat med hubLeveringNummer $hubLeveringNummer, " +
                    "Exception: ${it.message}", it)
        }.isSuccess

    override fun acknowledgeFailedEnvelope(hubLeveringNummer: String, errorMessage: String): Boolean =
        runCatching {
            ephytoDeliveryService
                .getClientConnection()
                .acknowledgeFailedEnvelopeReceipt(hubLeveringNummer, errorMessage)
            logger.error("Sender failed acknowledgment for hubLeveringNummer $hubLeveringNummer")

        }.onFailure {
            logger.warn("Kunne ikke bekrefte feilet mottak av sertifikat med hubLeveringNummer $hubLeveringNummer, " +
                    "Exception: ${it.message}", it)
        }.isSuccess
}
