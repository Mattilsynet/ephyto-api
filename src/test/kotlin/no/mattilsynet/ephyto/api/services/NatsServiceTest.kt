package no.mattilsynet.ephyto.api.services

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import io.nats.client.JetStreamSubscription
import no.mattilsynet.ephyto.api.mocks.dtos.EnvelopeFailedDtoMocker
import no.mattilsynet.ephyto.api.mocks.dtos.EnvelopeMetadataDtoMocker
import no.mattilsynet.fisk.libs.virtualnats.VirtualJetStream
import no.mattilsynet.fisk.libs.virtualnats.VirtualNats
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Profile
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@Profile("test")
@MockBean(SecretManagerServiceClient::class)
class NatsServiceTest {

    private lateinit var natsService: NatsService

    private val nats: VirtualNats = mock()

    private val jetStream: VirtualJetStream = mock()

    @BeforeEach
    fun setUp() {
        doReturn(jetStream).`when`(nats).jetStream(anyOrNull())
        natsService = NatsService(nats)
    }

    @Test
    fun `publishImportEnvelope kjoerer uten problemer med en envelope`() {
        // Given:
        val envelopeMetadataDto = EnvelopeMetadataDtoMocker.createEnvelopeMetadataDtoMock()
        doNothing().`when`(jetStream).subscribe(any(), any(), any())
        doReturn(null).`when`(jetStream).publish(any(), any())

        // When & Then:
        natsService.publishImportEnvelopeMetadata(envelopeMetadataDto)
    }

    @Test
    fun `publishImportEnvelopeFailed kjoerer uten problemer med en envelope`() {
        // Given:
        val envelopeFailedDtoMock = EnvelopeFailedDtoMocker.createEnvelopeFailedDtoMock()
        doNothing().`when`(jetStream).subscribe(any(), any(), any())
        doReturn(null).`when`(jetStream).publish(any(), any())

        // When & Then:
        natsService.publishImportEnvelopeFailed(envelopeFailedDtoMock)
    }
}
