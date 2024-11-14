package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.hub.ArrayOfEnvelopeForwarding
import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.HUBTrackingInfo

object EnvelopeMocker {

    fun createEnvelopeMock(
        content: String?,
        hubTrackingInfo: HUBTrackingInfo = HUBTrackingInfo.DELIVERED,
    ) =
        Envelope().also { envelope ->
            envelope.certificateType = 851
            envelope.certificateStatus = 70
            envelope.content = content
            envelope.forwardings = ArrayOfEnvelopeForwarding().also {
                it.envelopeForwarding.add(
                    EnvelopeForwardingMocker.createEnvelopeForwardingMock()
                )
            }
            envelope.from = "DK"
            envelope.hubDeliveryNumber = "1234567890"
            envelope.hubTrackingInfo = hubTrackingInfo.value()
            envelope.hubDeliveryErrorMessage = ""
            envelope.nppoCertificateNumber = "123ABC"
            envelope.to = "NO"
        }
}
