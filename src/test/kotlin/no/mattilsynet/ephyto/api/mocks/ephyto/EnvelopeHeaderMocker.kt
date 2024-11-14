package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.hub.EnvelopeHeader
import _int.ippc.ephyto.hub.HUBTrackingInfo

object EnvelopeHeaderMocker {

    fun createEnvelopeHeaderMock(
        hubTrackingInfo: HUBTrackingInfo = HUBTrackingInfo.DELIVERED,
    ) =
        EnvelopeHeader().also { envelopeHeader ->
            envelopeHeader.certificateType = 851
            envelopeHeader.certificateStatus = 70
            envelopeHeader.from = "DK"
            envelopeHeader.hubDeliveryNumber = "1234567890"
            envelopeHeader.hubTrackingInfo = hubTrackingInfo.value()
            envelopeHeader.hubDeliveryErrorMessage = ""
            envelopeHeader.nppoCertificateNumber = "123ABC"
            envelopeHeader.to = "NO"
        }

}
