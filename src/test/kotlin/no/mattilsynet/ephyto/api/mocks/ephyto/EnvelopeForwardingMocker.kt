package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.hub.EnvelopeForwarding

object EnvelopeForwardingMocker {

    fun createEnvelopeForwardingMock() =
        EnvelopeForwarding().also {
            it.code = "code"
            it.hubDeliveryNumber = "hubDeliveryNumber"
        }

}

