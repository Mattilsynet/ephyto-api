package no.mattilsynet.ephyto.api.mocks.dtos

import no.mattilsynet.ephyto.api.storage.blobstoragemetadata.v1.BlobStorageMetadata

object BlobStorageMetadataDtoMocker {

    fun createBlobStorageMetadataDtoMock(): BlobStorageMetadata =
        BlobStorageMetadata.newBuilder()
            .setStorageProviderType(BlobStorageMetadata.StorageProviderType.GCP)
            .setDatastoreType(BlobStorageMetadata.StorageProviderDatastoreType.GCP_BUCKET)
            .setDataStorageName("data-location-name")
            .setDataUrl("f47ac10b-58cc-4372-a567-0e02b2c3d479")
            .build()

}
