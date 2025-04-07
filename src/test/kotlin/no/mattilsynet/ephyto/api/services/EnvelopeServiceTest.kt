package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.hub.HUBTrackingInfo
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import no.mattilsynet.ephyto.api.imports.envelope.v1.EnvelopeMetadataDto
import no.mattilsynet.ephyto.api.mocks.domain.ValideringsresultatMocker.createValideringsresultatMock
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeMocker.createEnvelopeMock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@Suppress("UnusedPrivateProperty")
internal class EnvelopeServiceTest {

    private lateinit var envelopeService: EnvelopeService

    @MockitoBean
    private lateinit var gcpStorageService: GcpStorageService

    @MockitoBean
    private lateinit var natsService: NatsService

    @MockitoBean
    private lateinit var secretManagerServiceClient: SecretManagerServiceClient

    @BeforeEach
    fun setUp() {
        envelopeService = EnvelopeService(
            importEnvelopesBucketName = "test-bucket",
            gcpStorageService = gcpStorageService,
            natsService = natsService,
            profile = "test",
        )

        doReturn(mock()).`when`(gcpStorageService).lastOppEn(any(), any(), any(), any())
    }

    @Test
    fun `haandterNyEnvelope kjoerer uten problemer`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = "")
        doNothing().`when`(natsService).publishImportEnvelopeMetadata(any())

        val envelopeMetadataDtoArgumentCaptor = argumentCaptor<EnvelopeMetadataDto>()

        // When:
        envelopeService.haandterNyEnvelope(
            envelope = envelopeMock,
            valideringsresultat = createValideringsresultatMock(validatedOk = true),
        )

        // Then:
        verify(gcpStorageService, times(1)).lastOppEn(any(), any(), any(), any())

        verify(natsService, times(1))
            .publishImportEnvelopeMetadata(envelopeMetadataDtoArgumentCaptor.capture())
        envelopeMetadataDtoArgumentCaptor.allValues.first()
            .let { envelopeMetadataDto ->
                assertTrue(envelopeMetadataDto.blobStorageMetadata.dataUrl.contains(envelopeMock.nppoCertificateNumber))
            }
    }

    @Test
    fun `haandterNyEnvelope kaller ikke publishImportEnvelopeFailed ved DELIVERED`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = "")

        // When:
        envelopeService.haandterNyEnvelope(
            envelope = envelopeMock,
            valideringsresultat = createValideringsresultatMock(validatedOk = true),
        )

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
            .haandterNyEnvelope(
                envelope = envelopeMock,
                valideringsresultat = createValideringsresultatMock(
                    hubTrackingInfo = HUBTrackingInfo.DELIVERED_WITH_WARNINGS,
                    validatedOk = true,
                    ),
            )

        // Then:
        verify(natsService, times(1)).publishImportEnvelopeMetadata(any())
        verify(natsService, times(0)).publishImportEnvelopeFailed(any())
    }

    @Test
    fun `haandterNyEnvelope kaller publishImportEnvelopeFailed ved FAILED_DELIVERY`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = "")

        // When:
        envelopeService.haandterNyEnvelope(
            envelope = envelopeMock,
            valideringsresultat = createValideringsresultatMock(
                hubTrackingInfo = HUBTrackingInfo.FAILED_DELIVERY,
                validatedOk = false,
                ),
        )

        // Then:
        verify(natsService, times(1)).publishImportEnvelopeFailed(any())
    }

}
