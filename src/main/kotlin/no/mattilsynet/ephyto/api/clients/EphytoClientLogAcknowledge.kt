package no.mattilsynet.ephyto.api.clients

import no.mattilsynet.ephyto.api.domain.Valideringsresultat

class EphytoClientLogAcknowledge : EphytoClient() {

    override fun acknowledgeSuccessfulEnvelope(valideringsresultat: Valideringsresultat): Boolean {
        logger.info("Kun logging av successful acknowledgment for hubLeveringNummer " +
                valideringsresultat.hubLeveringNummer)
        return true
    }

    override fun acknowledgeFailedEnvelope(valideringsresultat: Valideringsresultat): Boolean {
        logger.info("Kun logging av feilet acknowledgment for hubLeveringNummer" +
                valideringsresultat.hubLeveringNummer)
        return true
    }

}
