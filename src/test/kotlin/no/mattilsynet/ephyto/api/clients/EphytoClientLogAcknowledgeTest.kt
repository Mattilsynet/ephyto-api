package no.mattilsynet.ephyto.api.clients

import _int.ippc.ephyto.IDeliveryService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
internal class EphytoClientLogAcknowledgeTest {

    @MockBean
    private lateinit var ephytoDeliveryService: EphytoDeliveryService

    @MockBean
    private lateinit var iDeliveryService: IDeliveryService

    private lateinit var ephytoClient: EphytoClientLogAcknowledge

    @BeforeEach
    fun setUp() {
        ephytoClient = EphytoClientLogAcknowledge()
        doReturn(iDeliveryService).`when`(ephytoDeliveryService).getClientConnection()
    }

    @Test
    fun `acknowledgeSuccesfulEnvelope sender suksessfull acknowledgment`() {
        // Given:
        doNothing().`when`(iDeliveryService).acknowledgeEnvelopeReceipt(any())

        // When:
        val ackErSendt = ephytoClient.acknowledgeSuccessfulEnvelope("hubLeveringNummer")

        // Then:
        assertEquals(true, ackErSendt)
        verify(iDeliveryService, never()).acknowledgeEnvelopeReceipt(any())
    }

    @Test
    fun `acknowledgeFailedEnvelope sender suksessfull failed acknowledgment`() {
        // Given:
        doReturn("").`when`(iDeliveryService).acknowledgeFailedEnvelopeReceipt(anyString(), anyString())

        // When:
        val ackErSendt = ephytoClient.acknowledgeFailedEnvelope("hubLeveringNummer", "error")

        // Then:
        assertEquals(true, ackErSendt)
        verify(iDeliveryService, never()).acknowledgeFailedEnvelopeReceipt(anyString(), anyString())
    }

}
