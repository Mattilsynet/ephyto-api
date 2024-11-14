package no.mattilsynet.ephyto.api.mocks.dtos

import no.mattilsynet.ephyto.api.imports.envelope.v1.ArrayOfEnvelopeForwardings
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeForwarding
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeHeaderDto

object EnvelopeHeaderDtoMocker {

    fun createEnvelopeHeaderDtoMock(): EnvelopeHeaderDto =
        EnvelopeHeaderDto.newBuilder()
            .setArrayOfEnvelopeForwardings(
                ArrayOfEnvelopeForwardings.newBuilder()
                    .addEnvelopeForwardings(
                        EnvelopeForwarding.newBuilder()
                            .setCode("code")
                            .setHubDeliveryNumber("hubDeliveryNumber")
                            .build()
                    )
            )
            .setCertificateStatus(1)
            .setCertificateType(2)
            .setFrom("from")
            .setHubDeliveryErrorMessage("hubDeliveryErrorMessage")
            .setHubDeliveryNumber("hubDeliveryNumber")
            .setHubTrackingInfo("hubTrackingInfo")
            .setNppoCertificateNumber("nppoCertificateNumber")
            .setTo("to")
            .build()

}
