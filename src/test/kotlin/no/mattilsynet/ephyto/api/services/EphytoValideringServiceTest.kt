package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.hub.HUBTrackingInfo
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeMocker
import no.mattilsynet.ephyto.api.validators.EphytoEnvelopeValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.Base64

internal class EphytoValideringServiceTest {

    @MockitoBean
    private val ephytoEnvelopeValidator = mock<EphytoEnvelopeValidator>()

    private lateinit var ephytoValideringService: EphytoValideringService

    private val xmlSertifikatContent = object {}.javaClass.classLoader
        .getResource("SPSCertificateContent.xml")!!.readText()

    @BeforeEach
    fun setUp() {
        ephytoValideringService = EphytoValideringService(
            ephytoEnvelopeValidator = ephytoEnvelopeValidator,
        )
    }

    @Test
    fun `hentOgHaandterNyEnvelope returnerer ok valideringsresultat dersom envelope har valid base64 content`() {
        // Given:
        val mockEnvelope = EnvelopeMocker.createEnvelopeMock(
            content = Base64.getEncoder().encodeToString(xmlSertifikatContent.toByteArray())
        )

        // When:
            ephytoValideringService.getValidationResultForEnvelope(mockEnvelope, mockEnvelope.hubDeliveryNumber)

        // Then:
        verify(ephytoEnvelopeValidator).getValideringsresultat(mockEnvelope)
    }

    @Test
    fun `hentOgHaandterNyEnvelope returnerer ikke-ok valideringsresultat dersom envelope er null`() {
        // Given:

        // When:
        val valideringsresultat =
            ephytoValideringService.getValidationResultForEnvelope(
                envelope = null,
                hubLeveringNummer = "hubDeliveryNumber",
            )

        // Then:
        with(valideringsresultat) {
            assertEquals(null, envelope)
            assertEquals("Could not read the envelope from the hub", errorMessage)
            assertEquals("hubDeliveryNumber", hubLeveringNummer)
            assertEquals(HUBTrackingInfo.ENVELOPE_NOT_EXISTS, hubTrackingInfo)
            assertFalse(validatedOk)
        }
    }

    @Test
    fun `hentOgHaandterNyEnvelope returnerer ikke-ok valideringsresultat dersom content er null`() {
        // Given:
        val envelopeMock = EnvelopeMocker.createEnvelopeMock(content = null)

        // When:
        val valideringsresultat =
            ephytoValideringService.getValidationResultForEnvelope(
                envelope = envelopeMock,
                hubLeveringNummer = envelopeMock.hubDeliveryNumber,
            )

        // Then:
        with(valideringsresultat) {
            assertEquals(envelopeMock, envelope)
            assertEquals("The content of the envelope from the hub is empty", errorMessage)
            assertEquals(envelopeMock.hubDeliveryNumber, hubLeveringNummer)
            assertEquals(HUBTrackingInfo.DELIVERED_NOT_READABLE, hubTrackingInfo)
            assertFalse(validatedOk)
        }
    }
}
