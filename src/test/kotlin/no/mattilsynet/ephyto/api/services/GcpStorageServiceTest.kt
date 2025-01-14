package no.mattilsynet.ephyto.api.services

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.InputStream

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@Suppress("UnusedPrivateProperty")
internal class GcpStorageServiceTest {

    private lateinit var gcpStorageService: GcpStorageService

    @MockitoBean
    private lateinit var storage: Storage

    @MockitoBean
    private lateinit var bucket: Bucket

    @MockitoBean
    private lateinit var blob: Blob

    @MockitoBean
    private lateinit var secretManagerServiceClient: SecretManagerServiceClient

    private val bucketName = "bucketName"

    @BeforeEach
    fun setUp() {
        gcpStorageService = GcpStorageService(
            storage = storage,
        )

        blob = mock()
    }

    @Test
    fun `test at getBucket returnerer null naar bucket ikke finnes`() {
        // Given:
        doThrow(StorageException(404, "Not found"))
            .`when`(storage).get(any<String>())

        // When:
        val bucket = gcpStorageService.getBucket(bucketName)

        // Then:
        assertNull(bucket)
    }

    @Test
    fun `test at getBucket returnerer bucket naar bucket finnes`() {
        // Given:
        doReturn(mock<Bucket>())
            .`when`(storage).get(any<String>())

        // When:
        val bucket = gcpStorageService.getBucket(bucketName)

        // Then:
        assertNotNull(bucket)
    }

    @Test
    fun `test at lastOppEn kjoerer uten problemer`() {
        // Given:
        doReturn(bucket)
            .`when`(storage).get(any<String>())
        doReturn(blob)
            .`when`(bucket).create(anyString(), any<ByteArray>())


        // When:
        val blobben = gcpStorageService.lastOppEn(
            bucketName = bucketName,
            contentBytes = "HELLO WORLD".toByteArray(),
            dataUrl = "dataUrl",
            hubLeveringNummer = "hubLeveringNummer",
        )

        // Then:
        assertNotNull(blobben)
    }

    @Test
    fun `test at lastOppEn returnerer null naar nedlasting feiler`() {
        // Given:
        doReturn(bucket)
            .`when`(storage).get(any<String>())
        doReturn(null)
            .`when`(bucket).create(anyString(), any<InputStream>(), anyString())


        // When:
        val blobben = gcpStorageService.lastOppEn(
            bucketName = bucketName,
            contentBytes = "HELLO WORLD".toByteArray(),
            dataUrl = "dataUrl",
            hubLeveringNummer = "hubLeveringNummer",
        )

        // Then:
        assertNull(blobben)
    }

}
