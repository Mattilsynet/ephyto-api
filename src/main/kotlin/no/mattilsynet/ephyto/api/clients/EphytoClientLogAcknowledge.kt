package no.mattilsynet.ephyto.api.clients

class EphytoClientLogAcknowledge : EphytoClient() {

    override fun acknowledgeSuccessfulEnvelope(hubLeveringNummer: String): Boolean {
        logger.info("Kun logging av successful acknowledgment for hubLeveringNummer $hubLeveringNummer")
        return true
    }

    override fun acknowledgeFailedEnvelope(hubLeveringNummer: String, errorMessage: String): Boolean {
        logger.info("Kun logging av feilet acknowledgment for hubLeveringNummer $hubLeveringNummer")
        return true
    }

}
