package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.HubWebException_Exception
import _int.ippc.ephyto.IDeliveryService
import no.mattilsynet.ephyto.api.clients.EphytoDeliveryService
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeMocker.createEnvelopeMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
internal class EphytoGetSingleImportEnvelopeServiceTest {

    private lateinit var ephytoGetSingleImportEnvelopeService: EphytoGetSingleImportEnvelopeService

    @MockitoBean
    private lateinit var iDeliveryService: IDeliveryService

    @MockitoBean
    private lateinit var ephytoDeliveryService: EphytoDeliveryService

    @BeforeEach
    fun setUp() {
        ephytoGetSingleImportEnvelopeService = EphytoGetSingleImportEnvelopeService(
            ephytoDeliveryService = ephytoDeliveryService,
        )
        doReturn(iDeliveryService).`when`(ephytoDeliveryService).getClientConnection()
    }

    @Test
    fun `test getSingleImportEnvelope returnerer envelope paa riktig format`() {
        // Given:
        doReturn(createEnvelopeMock(content = "<XML content>"))
            .`when`(iDeliveryService).pullSingleImportEnvelope(anyString())

        // When:
        val envelope = ephytoGetSingleImportEnvelopeService.getSingleImportEnvelope("sertifikatId")

        // Then:
        assertNotNull(envelope)
        with(envelope) {
            assertEquals("DK", from)
            assertEquals("NO", to)
            assertEquals("1234567890", hubDeliveryNumber)
            assertNotNull(content)
        }
    }

    @Test
    fun `getSingleImportEnvelope kaster feil dersom pullSingleImportEnvelope feiler`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).pullSingleImportEnvelope(any())

        // When:
        assertThrows<HubWebException_Exception> {
            ephytoGetSingleImportEnvelopeService.getSingleImportEnvelope("sertifikatId")
        }.message
            .let { message ->
                assertEquals("feil kastet", message)
            }
    }
}
