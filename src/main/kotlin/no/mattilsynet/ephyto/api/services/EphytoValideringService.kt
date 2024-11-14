package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.HUBTrackingInfo
import no.mattilsynet.ephyto.api.domain.Valideringsresultat
import no.mattilsynet.ephyto.api.validators.EphytoEnvelopeValidator
import org.springframework.stereotype.Service

@Service
class EphytoValideringService(
    private val ephytoEnvelopeValidator: EphytoEnvelopeValidator,
) {

    fun getValidationResultForEnvelope(
        envelope: Envelope?,
        hubLeveringNummer: String,
    ): Valideringsresultat =
        when {
            envelope == null -> {
                Valideringsresultat(
                    envelope = null,
                    errorMessage = "Could not read the envelope from the hub",
                    hubLeveringNummer = hubLeveringNummer,
                    hubTrackingInfo = HUBTrackingInfo.ENVELOPE_NOT_EXISTS,
                    validatedOk = false,
                )
            }

            envelope.content.isNullOrBlank() -> {
                Valideringsresultat(
                    envelope = envelope,
                    errorMessage = "The content of the envelope from the hub is empty",
                    hubLeveringNummer = hubLeveringNummer,
                    hubTrackingInfo = HUBTrackingInfo.DELIVERED_NOT_READABLE,
                    validatedOk = false,
                )
            }

            else -> ephytoEnvelopeValidator.getValideringsresultat(envelope = envelope)
        }
}
