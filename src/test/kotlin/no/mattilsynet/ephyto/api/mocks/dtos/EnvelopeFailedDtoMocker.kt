package no.mattilsynet.ephyto.api.mocks.dtos

import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeFailedDto
import no.mattilsynet.ephyto.api.mocks.dtos.BlobStorageMetadataDtoMocker.createBlobStorageMetadataDtoMock

object EnvelopeFailedDtoMocker {

    fun createEnvelopeFailedDtoMock(): EnvelopeFailedDto =
        EnvelopeFailedDto.newBuilder()
            .setBlobStorageMetadata(createBlobStorageMetadataDtoMock())
            .setNppoCertificateNumber("nppoSertifikatnr")
            .setHubDeliveryNumber("hubDeliveryNumber")
            .build()

}
