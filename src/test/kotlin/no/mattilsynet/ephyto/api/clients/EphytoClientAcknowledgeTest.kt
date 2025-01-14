package no.mattilsynet.ephyto.api.clients

import _int.ippc.ephyto.HubWebException_Exception
import _int.ippc.ephyto.IDeliveryService
import _int.ippc.ephyto.IntendedUse
import _int.ippc.ephyto.MeanOfTransport
import _int.ippc.ephyto.Statement
import _int.ippc.ephyto.TreatmentType
import _int.ippc.ephyto.hub.ArrayOfEnvelopeHeader
import _int.ippc.ephyto.hub.ArrayOfNppo
import _int.ippc.ephyto.hub.Nppo
import no.mattilsynet.ephyto.api.mocks.domain.ValideringsresultatMocker.createValideringsresultatMock
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeHeaderMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeMocker.createEnvelopeMock
import no.mattilsynet.ephyto.api.mocks.ephyto.NppoMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.ValidationResultMocker.createValidationResultMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
internal class EphytoClientAcknowledgeTest {

    @MockitoBean
    private lateinit var ephytoDeliveryService: EphytoDeliveryService

    @MockitoBean
    private lateinit var iDeliveryService: IDeliveryService

    @InjectMocks
    private lateinit var ephytoClient: EphytoClientAcknowledge

    @InjectMocks
    private lateinit var ephytoKodeverkClient: EphytoKodeverkClient

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        doReturn(iDeliveryService).`when`(ephytoDeliveryService).getClientConnection()
    }

    @Test
    fun `acknowledgeEnvelope acknowledger naar validatedOk er true`() {
        // Given:
        val valideringsresultatMock = createValideringsresultatMock(validatedOk = true)

        // When:
        ephytoClient.acknowledgeEnvelope(valideringsresultatMock)

        // Then:
        verify(iDeliveryService).acknowledgeEnvelopeReceipt(valideringsresultatMock.hubLeveringNummer)
    }

    @Test
    fun `acknowledgeEnvelope acknowledger failed naar validatedOk er false`() {
        // Given:
        val valideringsresultatMock = createValideringsresultatMock(validatedOk = false)

        // When:
        ephytoClient.acknowledgeEnvelope(valideringsresultatMock)

        // Then:
        verify(iDeliveryService, never()).acknowledgeEnvelopeReceipt(valideringsresultatMock.hubLeveringNummer)
    }

    @Test
    fun `validatePhytoXml returnerer tom liste dersom det blir kastet en exception`() {
        // Given:
        doThrow(HubWebException_Exception("Feil"))
            .`when`(iDeliveryService).validatePhytoXML(anyString())

        // When
        val validationResults = ephytoClient.validatePhytoXml("phytoXml")

        // Then:
        assertTrue(validationResults.isEmpty())
    }

    @Test
    fun `validatePhytoXml returnerer liste med ValidationResult`() {
        // Given:
        doReturn(listOf(createValidationResultMock()))
            .`when`(iDeliveryService).validatePhytoXML(anyString())

        // When:
        val validationResult = ephytoClient.validatePhytoXml("phytoXml")

        // Then:
        assertNotNull(validationResult)
        assertFalse(validationResult.isEmpty())
    }

    @Test
    fun `acknowledgeSuccesfulEnvelope sender suksessfull acknowledgment`() {
        // Given:
        doNothing().`when`(iDeliveryService).acknowledgeEnvelopeReceipt(any())

        // When:
        val ackErSendt = ephytoClient.acknowledgeSuccessfulEnvelope("hubLeveringNummer")

        // Then:
        assertEquals(true, ackErSendt)
        verify(iDeliveryService).acknowledgeEnvelopeReceipt(any())
    }

    @Test
    fun `acknowledgeSuccesfulEnvelope kaster exception naar ephytoDeliveryService feiler`() {
        // Given
        doThrow(HubWebException_Exception("Feil"))
            .`when`(iDeliveryService).acknowledgeEnvelopeReceipt(anyString())

        // When:
        val ackErSendt = ephytoClient.acknowledgeSuccessfulEnvelope("hubLeveringNummer")

        // Then:
        assertEquals(false, ackErSendt)
    }

    @Test
    fun `acknowledgeFailedEnvelope sender suksessfull failed acknowledgment`() {
        // Given:
        doReturn("").`when`(iDeliveryService).acknowledgeFailedEnvelopeReceipt(anyString(), anyString())

        // When:
        val ackErSendt = ephytoClient.acknowledgeFailedEnvelope("hubLeveringNummer", "error")

        // Then:
        assertEquals(true, ackErSendt)
        verify(iDeliveryService).acknowledgeFailedEnvelopeReceipt(anyString(), anyString())
    }

    @Test
    fun `acknowledgeFailedEnvelope kaster exception naar ephytoDeliveryService feiler`() {
        // Given
        doThrow(HubWebException_Exception("Feil"))
            .`when`(iDeliveryService).acknowledgeFailedEnvelopeReceipt(anyString(), anyString())

        // When:
        val ackErSendt = ephytoClient.acknowledgeFailedEnvelope("hubLeveringNummer", "error")

        // Then:
        assertEquals(false, ackErSendt)
    }
    @Test
    fun `test ephytoClient returnerer null dersom det blir kastet en exception`() {
        // Given:
        given(ephytoDeliveryService.getClientConnection()).willAnswer {
            throw HubWebException_Exception("feil")
        }

        // When & Then:
        assertThrows<HubWebException_Exception> {
            ephytoDeliveryService.getClientConnection()
        }
        assertNull(ephytoClient.getSingleImportEnvelopeOrNull(anyString()))
    }

    @Test
    fun `test getSingleImportEnvelope returnerer envelope paa riktig format`() {
        // Given:
        doReturn(createEnvelopeMock(content = "<XML content>"))
            .`when`(iDeliveryService).pullSingleImportEnvelope(anyString())

        // When:
        val envelope = ephytoClient.getSingleImportEnvelopeOrNull("sertifikatId")

        // Then:
        assertNotNull(envelope)
        with(envelope!!) {
            assertEquals("DK", from)
            assertEquals("NO", to)
            assertEquals("1234567890", hubDeliveryNumber)
            assertNotNull(content)
        }
    }

    @Test
    fun `test getSingleImportEnvelope returnerer null dersom pullSingleImportEnvelope kaster feil`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).pullSingleImportEnvelope(any())

        // When:
        val resultat = ephytoClient.getSingleImportEnvelopeOrNull("sertifikatId")

        // Then:
        assertNull(resultat)
    }

    @Test
    fun `test getImportEnvelopeHeaders returnerer envelopeHeaders paa riktig format`() {
        // Given:
        doReturn(ArrayOfEnvelopeHeader()
            .also { arrayOfEnvelopeHeader ->
                arrayOfEnvelopeHeader.envelopeHeader.add(
                    EnvelopeHeaderMocker.createEnvelopeHeaderMock()
                )
            }
        ).`when`(iDeliveryService).getImportEnvelopeHeaders(any())

        // When:
        val envelopeHeaders = ephytoClient.getImportEnvelopeHeaders()

        // Then:
        assertNotNull(envelopeHeaders)
        assertFalse(envelopeHeaders!!.isEmpty())
        with(envelopeHeaders[0]) {
            assertEquals("DK", from)
            assertEquals("NO", to)
        }
    }

    @Test
    fun `test  returnerer meansOfTransport paa riktig format`() {
        // Given:
        doReturn(
            listOf(
                MeanOfTransport()
                    .also { meanOfTransport ->
                        meanOfTransport.modeCode = 3
                        meanOfTransport.usedTransportMean = "Road transport"
                    }
            )
        ).`when`(iDeliveryService).meanOfTransports

        // When:
        val meanOfTransports = ephytoKodeverkClient.getMeanOfTransports()

        // Then:
        assertNotNull(meanOfTransports)
        assertFalse(meanOfTransports.isEmpty())
        with(meanOfTransports[0]) {
            assertEquals("Road transport", usedTransportMean)
            assertEquals(3, modeCode)
        }
    }

    @Test
    fun `test getImportEnvelopeHeaders returnerer null dersom getImportEnvelopeHeaders kaster feil`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).getImportEnvelopeHeaders(any())

        // When:
        val resultat = ephytoClient.getImportEnvelopeHeaders()

        // Then:
        assertNull(resultat)
    }

    @Test
    fun `test getMeanOfTransports returnerer tom liste dersom meanOfTransports kaster feil`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).meanOfTransports

        // When:
        val resultat = ephytoKodeverkClient.getMeanOfTransports()

        // Then:
        assertTrue(resultat.isEmpty())
    }

    @Test
    fun `test getStatements returnerer rett`() {
        // Given:
        doReturn(
            listOf(
                mock<Statement>(),
                mock<Statement>(),
                mock<Statement>(),
            )
        ).`when`(iDeliveryService).statements

        // When:
        val statements = ephytoKodeverkClient.getStatements()

        // Then:
        assertNotNull(statements)
        assertFalse(statements.isEmpty())
    }

    @Test
    fun `test getStatements returnerer null dersom statements kaster feil`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).statements

        // When:
        val resultat = ephytoKodeverkClient.getStatements()

        // Then:
        assertTrue(resultat.isEmpty())
    }

    @Test
    fun `test getTreatmentTypes returnerer rett`() {
        // Given:
        doReturn(
            listOf(
                mock<TreatmentType>(),
                mock<TreatmentType>(),
                mock<TreatmentType>(),
            )
        ).`when`(iDeliveryService).treatmentTypes

        // When:
        val treatmentTypes = ephytoKodeverkClient.getTreatmentTypes()

        // Then:
        assertNotNull(treatmentTypes)
        assertFalse(treatmentTypes.isEmpty())
    }

    @Test
    fun `test getTreatmentTypes returnerer null dersom treatmentTypes kaster feil`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).treatmentTypes

        // When:
        val resultat = ephytoKodeverkClient.getTreatmentTypes()

        // Then:
        assertTrue(resultat.isEmpty())
    }

    @Test
    fun `test getIndendedUse returnerer rett`() {
        // Given:
        doReturn(
            listOf(
                mock<IntendedUse>(),
                mock<IntendedUse>(),
                mock<IntendedUse>(),
            )
        ).`when`(iDeliveryService).intendedUses

        // When:
        val intendedUses = ephytoKodeverkClient.getIndendedUse()

        // Then:
        assertNotNull(intendedUses)
        assertFalse(intendedUses.isEmpty())
    }

    @Test
    fun `test getIndendedUse returnerer null dersom intendedUses kaster feil`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).intendedUses

        // When:
        val resultat = ephytoKodeverkClient.getIndendedUse()

        // Then:
        assertTrue(resultat.isEmpty())
    }

    @Test
    fun `getActiveNppos returnerer null dersom getActiveNppos kaster feil`() {
        // Given:
        doThrow(
            HubWebException_Exception("feil kastet")
        ).`when`(iDeliveryService).activeNppos

        // When:
        val nppo = ephytoKodeverkClient.getActiveNppos()

        // Then:
        assertNull(nppo)
    }

    @Test
    fun `getActiveNppos returnerer rett`() {
        // Given:
        val nppoList = ArrayOfNppo()
        nppoList.nppo.addAll(listOf(
            mock<Nppo>(),
            mock<Nppo>(),
            NppoMocker.createNppoMockMedLandkode("NO"),
        ))

        doReturn(nppoList).`when`(iDeliveryService).activeNppos

        // When:
        val arrayOfNppo = ephytoKodeverkClient.getActiveNppos()

        // Then:
        assertNotNull(arrayOfNppo)
        assertNotNull(arrayOfNppo!!.nppo.find { it.country == "NO" })
    }
}
