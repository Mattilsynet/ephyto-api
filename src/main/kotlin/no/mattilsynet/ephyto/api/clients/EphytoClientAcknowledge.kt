package no.mattilsynet.ephyto.api.clients

import no.mattilsynet.ephyto.api.domain.Valideringsresultat
import org.springframework.stereotype.Component

@Component
class EphytoClientAcknowledge : EphytoClient() {

    override fun acknowledgeSuccessfulEnvelope(valideringsresultat: Valideringsresultat): Boolean =
        runCatching {
            when {
                valideringsresultat.errorMessage.isNullOrBlank() -> {
                    ephytoDeliveryService
                        .getClientConnection()
                        .acknowledgeEnvelopeReceipt(valideringsresultat.hubLeveringNummer)
                    logger.info(
                        "Sender successful acknowledgment for hubLeveringNummer " +
                                valideringsresultat.hubLeveringNummer
                    )
                }

                else -> {
                    ephytoDeliveryService
                        .getClientConnection()
                        .advancedAcknowledgeEnvelopeReceipt(
                            valideringsresultat.hubLeveringNummer,
                            valideringsresultat.errorMessage,
                        )
                    logger.info(
                        "Sender advanced acknowledgment med feilmelding for hubLeveringNummer " +
                                valideringsresultat.hubLeveringNummer
                    )
                }
            }

        }.onFailure {
            logger.warn(
                "Kunne ikke bekrefte ok mottak av sertifikat med hubLeveringNummer $valideringsresultat, " +
                        "Exception: ${it.message}", it
            )
        }.isSuccess

    override fun acknowledgeFailedEnvelope(valideringsresultat: Valideringsresultat): Boolean =
        with(valideringsresultat) {
            runCatching {
                ephytoDeliveryService
                    .getClientConnection()
                    .acknowledgeFailedEnvelopeReceipt(valideringsresultat.hubLeveringNummer, errorMessage)
                logger.error("Sender failed acknowledgment for hubLeveringNummer " +
                        valideringsresultat.hubLeveringNummer)

            }.onFailure {
                logger.error(
                    "Kunne ikke bekrefte feilet mottak av sertifikat med hubLeveringNummer" +
                            " ${valideringsresultat.hubLeveringNummer}, Exception: ${it.message}", it
                )
            }.isSuccess
        }
}
