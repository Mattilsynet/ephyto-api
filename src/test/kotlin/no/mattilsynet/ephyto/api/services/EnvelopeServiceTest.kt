package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.hub.HUBTrackingInfo
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeMetadataDto
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeMocker.createEnvelopeMock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@MockBean(SecretManagerServiceClient::class)
class EnvelopeServiceTest {

    private lateinit var envelopeService: EnvelopeService

    @MockBean
    private lateinit var gcpStorageService: GcpStorageService

    @MockBean
    private lateinit var natsService: NatsService

    @BeforeEach
    fun setUp() {
        envelopeService = EnvelopeService(
            importEnvelopesBucketName = "test-bucket",
            gcpStorageService = gcpStorageService,
            natsService = natsService,
            profile = "test",
        )
    }

    @Test
    fun `haandterNyEnvelope kjoerer uten problemer`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = "")
        doReturn(null).`when`(gcpStorageService).lastOppEn(any(), any(), any(), any())
        doNothing().`when`(natsService).publishImportEnvelopeMetadata(any())

        val envelopeMetadataDtoArgumentCaptor = argumentCaptor<EnvelopeMetadataDto>()

        // When:
        envelopeService.haandterNyEnvelope(envelope = envelopeMock, hubTrackingInfo = HUBTrackingInfo.DELIVERED)

        // Then:
        verify(gcpStorageService, times(1)).lastOppEn(any(), any(), any(), any())

        verify(natsService, times(1))
            .publishImportEnvelopeMetadata(envelopeMetadataDtoArgumentCaptor.capture())
        envelopeMetadataDtoArgumentCaptor.allValues.first()
            .let {  envelopeMetadataDto ->
                assertTrue(envelopeMetadataDto.blobStorageMetadata.dataUrl.contains(envelopeMock.nppoCertificateNumber))
            }
    }

    @Test
    fun `haandterNyEnvelope kaller ikke publishImportEnvelopeFailed ved DELIVERED`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = "")

        // When:
        envelopeService.haandterNyEnvelope(envelope = envelopeMock, hubTrackingInfo = HUBTrackingInfo.DELIVERED)

        // Then:
        verify(natsService, times(1)).publishImportEnvelopeMetadata(any())
        verify(natsService, times(0)).publishImportEnvelopeFailed(any())
    }

    @Test
    fun `haandterNyEnvelope kaller ikke publishImportEnvelopeFailed ved DELIVERED_WITH_WARNINGS`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = "")

        // When:
        envelopeService
            .haandterNyEnvelope(envelope = envelopeMock, hubTrackingInfo = HUBTrackingInfo.DELIVERED_WITH_WARNINGS)

        // Then:
        verify(natsService, times(1)).publishImportEnvelopeMetadata(any())
        verify(natsService, times(0)).publishImportEnvelopeFailed(any())
    }

    @Test
    fun `haandterNyEnvelope kaller publishImportEnvelopeFailed ved FAILED_DELIVERY`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = "")

        // When:
        envelopeService.haandterNyEnvelope(envelope = envelopeMock, hubTrackingInfo = HUBTrackingInfo.FAILED_DELIVERY)

        // Then:
        verify(natsService, times(1)).publishImportEnvelopeMetadata(any())
        verify(natsService, times(1)).publishImportEnvelopeFailed(any())
    }

}
