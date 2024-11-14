package no.mattilsynet.ephyto.api.services

import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GcpStorageService(
    private val storage: Storage,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getBucket(bucketName: String): Bucket? =
        try {
            storage[bucketName]
        } catch (e: StorageException) {
            logger.error("Bucket $bucketName finnes ikke. Exception: ${e.message}", e)
            null
        }

    fun lastOppEn(
        bucketName: String,
        contentBytes: ByteArray,
        dataUrl: String,
        hubLeveringNummer: String,
    ): Blob? =
        getBucket(bucketName)?.let { bucket ->
            try {
                bucket.create(
                    dataUrl,
                    contentBytes,
                )?.also { blob ->
                    logger.info("Filen ${blob.name} er skrevet til: $bucketName/$dataUrl")
                } ?: run {
                    logger.error("Filen for $hubLeveringNummer kunne ikke skrives til bucket: $bucketName")
                    null
                }
            } catch (e: StorageException) {
                logger.error("Filen for $hubLeveringNummer kunne ikke skrives til bucket: $bucketName. " +
                        "Exception: ${e.message}")
                null
            }
        }
    
}

