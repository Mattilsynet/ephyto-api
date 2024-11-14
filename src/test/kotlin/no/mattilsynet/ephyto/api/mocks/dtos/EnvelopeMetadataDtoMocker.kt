package no.mattilsynet.ephyto.api.mocks.dtos

import com.google.protobuf.Timestamp
import java.time.Instant
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeMetadataDto
import no.mattilsynet.ephyto.api.mocks.dtos.BlobStorageMetadataDtoMocker.createBlobStorageMetadataDtoMock
import no.mattilsynet.ephyto.api.mocks.dtos.EnvelopeHeaderDtoMocker.createEnvelopeHeaderDtoMock

object EnvelopeMetadataDtoMocker {

    fun createEnvelopeMetadataDtoMock(): EnvelopeMetadataDto =
        EnvelopeMetadataDto.newBuilder()
            .setBlobStorageMetadata(createBlobStorageMetadataDtoMock())
            .setEnvelopeHeader(createEnvelopeHeaderDtoMock())
            .setReceivedAt(
                Instant.now().let { instant ->
                    Timestamp.newBuilder()
                        .setSeconds(instant.epochSecond)
                        .setNanos(instant.nano)
                        .build()
                }
            )
            .build()

}
