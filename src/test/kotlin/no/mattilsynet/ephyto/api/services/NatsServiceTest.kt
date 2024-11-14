package no.mattilsynet.ephyto.api.services

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import io.nats.client.JetStreamSubscription
import no.mattilsynet.ephyto.api.mocks.dtos.EnvelopeFailedDtoMocker
import no.mattilsynet.ephyto.api.mocks.dtos.EnvelopeMetadataDtoMocker
import no.mattilsynet.fisk.libs.reactivenats.ReactiveJetStream
import no.mattilsynet.fisk.libs.reactivenats.ReactiveNats
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Profile
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@Profile("test")
@MockBean(SecretManagerServiceClient::class)
class NatsServiceTest {

    private lateinit var natsService: NatsService

    @MockBean
    private lateinit var reactiveNats: ReactiveNats

    @MockBean
    private lateinit var reactiveJetStream: ReactiveJetStream

    @BeforeEach
    fun setUp() {
        natsService = NatsService(reactiveNats)
    }

    @Test
    fun `publishImportEnvelope kjoerer uten problemer med en envelope`() {
        // Given:
        val envelopeMetadataDto = EnvelopeMetadataDtoMocker.createEnvelopeMetadataDtoMock()
        doReturn(reactiveJetStream).`when`(reactiveNats).jetStream()
        doReturn(Mono.empty<JetStreamSubscription>()).`when`(reactiveJetStream).subscribe(any(), any(), any(), any())
        doReturn(Mono.empty<JetStreamSubscription>()).`when`(reactiveJetStream).publish(any(), any())

        // When & Then:
        natsService.publishImportEnvelopeMetadata(envelopeMetadataDto)
    }

    @Test
    fun `publishImportEnvelopeFailed kjoerer uten problemer med en envelope`() {
        // Given:
        val envelopeFailedDtoMock = EnvelopeFailedDtoMocker.createEnvelopeFailedDtoMock()
        doReturn(reactiveJetStream).`when`(reactiveNats).jetStream()
        doReturn(Mono.empty<JetStreamSubscription>()).`when`(reactiveJetStream).subscribe(any(), any(), any(), any())
        doReturn(Mono.empty<JetStreamSubscription>()).`when`(reactiveJetStream).publish(any(), any())

        // When & Then:
        natsService.publishImportEnvelopeFailed(envelopeFailedDtoMock)
    }
}
