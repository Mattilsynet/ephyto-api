package no.mattilsynet.ephyto.api.validators

import _int.ippc.ephyto.ValidationLevel
import _int.ippc.ephyto.ValidationResult
import _int.ippc.ephyto.hub.HUBTrackingInfo
import no.mattilsynet.ephyto.api.clients.EphytoClientAcknowledge
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeMocker.createEnvelopeMock
import no.mattilsynet.ephyto.api.mocks.ephyto.ValidationResultMocker.createValidationResultMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.Base64

class EphytoEnvelopeValidatorTest {

    @MockBean
    private lateinit var ephytoClient: EphytoClientAcknowledge

    private lateinit var ephytoEnvelopeValidator: EphytoEnvelopeValidator

    private val xmlSertifikatContent = object {}.javaClass.classLoader
        .getResource("SPSCertificateContent.xml")?.readText()

    @BeforeEach
    fun setUp() {
        ephytoClient = mock()
        ephytoEnvelopeValidator = EphytoEnvelopeValidator(
            ephytoClient = ephytoClient
        )
    }

    @Test
    fun `getValideringsresultat returnerer DELIVERED naar envelope er ok`() {
        // Given:
        val mockEnvelope = createEnvelopeMock(content = xmlSertifikatContent)

        doReturn(emptyList<ValidationResult>()).`when`(ephytoClient).validatePhytoXml(mockEnvelope.content)

        // When:
        val valideringsresultat = ephytoEnvelopeValidator.getValideringsresultat(mockEnvelope)

        // Then:
        assertNotNull(valideringsresultat)
        with(valideringsresultat) {
            assertEquals(mockEnvelope, envelope)
            assertEquals(null, errorMessage)
            assertTrue(validatedOk)
            assertEquals(mockEnvelope.hubDeliveryNumber, hubLeveringNummer)
            assertEquals(HUBTrackingInfo.DELIVERED, hubTrackingInfo)
        }
    }

    @Test
    fun `getValideringsresultat returnerer DELIVERED naar envelope har base64 encoded content`() {
        // Given:
        val mockEnvelope = createEnvelopeMock(
            content = Base64.getEncoder().encodeToString(xmlSertifikatContent?.toByteArray())
        )

        doReturn(emptyList<ValidationResult>()).`when`(ephytoClient).validatePhytoXml(mockEnvelope.content)

        // When:
        val valideringsresultat = ephytoEnvelopeValidator.getValideringsresultat(mockEnvelope)

        // Then:
        assertNotNull(valideringsresultat)
        with(valideringsresultat) {
            assertEquals(mockEnvelope, envelope)
            assertEquals(null, errorMessage)
            assertTrue(validatedOk)
            assertEquals(mockEnvelope.hubDeliveryNumber, hubLeveringNummer)
            assertEquals(HUBTrackingInfo.DELIVERED, hubTrackingInfo)
        }
    }

    @Test
    fun `getValideringsresultat returnerer null riktig feilmelding naar envelopens innhold er i feil format`() {
        // Given:
        val mockEnvelope = createEnvelopeMock(content = "tull og tøys>")

        // When:
        val valideringsresultat = ephytoEnvelopeValidator.getValideringsresultat(mockEnvelope)

        // Then:
        assertNotNull(valideringsresultat)
        with(valideringsresultat) {
            assertEquals(mockEnvelope, envelope)
            assertEquals("The XML content in the envelope is not readable", errorMessage)
            assertFalse(validatedOk)
            assertEquals(mockEnvelope.hubDeliveryNumber, hubLeveringNummer)
            assertEquals(HUBTrackingInfo.DELIVERED_NOT_READABLE, hubTrackingInfo)
        }
    }

    @Test
    fun `getValideringsresultat returnerer riktig ved alvorlige valideringsfeil fra ephyto`() {
        // Given:
        val mockEnvelope = createEnvelopeMock(content = xmlSertifikatContent)

        doReturn(
            listOf(
                createValidationResultMock(level = ValidationLevel.SEVERE),
                createValidationResultMock(level = ValidationLevel.SEVERE),
                createValidationResultMock(level = ValidationLevel.SEVERE),
            )
        ).`when`(ephytoClient).validatePhytoXml(mockEnvelope.content)

        // When:
        val valideringsresultat = ephytoEnvelopeValidator.getValideringsresultat(mockEnvelope)

        // Then:
        assertNotNull(valideringsresultat)
        with(valideringsresultat) {
            assertEquals(mockEnvelope, envelope)
            assertTrue(valideringsresultat.errorMessage!!.contains("SEVERE"))
            assertTrue(validatedOk)
            assertEquals(mockEnvelope.hubDeliveryNumber, hubLeveringNummer)
            assertEquals(HUBTrackingInfo.DELIVERED_WITH_WARNINGS, hubTrackingInfo)
        }
    }

}
