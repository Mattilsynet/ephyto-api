package no.mattilsynet.ephyto.api.mocks.domain

import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.HUBTrackingInfo
import no.mattilsynet.ephyto.api.domain.Valideringsresultat

object ValideringsresultatMocker {

    fun createValideringsresultatMock(
        envelopeMock: Envelope? = null,
        errorMessage: String = "",
        hubTrackingInfo: HUBTrackingInfo = HUBTrackingInfo.DELIVERED,
        validatedOk: Boolean = true,
    ) = Valideringsresultat(
        envelope = envelopeMock,
        errorMessage = errorMessage,
        hubLeveringNummer = envelopeMock?.hubDeliveryNumber ?: "hubDeliveryNumber",
        hubTrackingInfo = hubTrackingInfo,
        validatedOk = validatedOk,
    )

}
