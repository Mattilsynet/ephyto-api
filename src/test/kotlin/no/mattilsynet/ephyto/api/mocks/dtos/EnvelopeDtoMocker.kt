package no.mattilsynet.ephyto.api.mocks.dtos

import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeDto
import no.mattilsynet.ephyto.api.mocks.dtos.EnvelopeMetadataDtoMocker.createEnvelopeMetadataDtoMock

object EnvelopeDtoMocker {

    fun createEnvelopeDtoMock(): EnvelopeDto =
        EnvelopeDto.newBuilder()
            .setEnvelopeMetadata(createEnvelopeMetadataDtoMock())
            .setContent("")
            .build()


}

