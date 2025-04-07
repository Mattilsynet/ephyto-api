package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.IntendedUse
import _int.ippc.ephyto.MeanOfTransport
import _int.ippc.ephyto.Statement
import _int.ippc.ephyto.TreatmentType
import _int.ippc.ephyto.hub.ArrayOfNppo
import _int.ippc.ephyto.hub.HUBTrackingInfo
import _int.ippc.ephyto.hub.Nppo
import no.mattilsynet.ephyto.api.clients.EphytoClient
import no.mattilsynet.ephyto.api.clients.EphytoKodeverkClient
import no.mattilsynet.ephyto.api.logic.vask
import no.mattilsynet.ephyto.api.mocks.domain.ValideringsresultatMocker.createValideringsresultatMock
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeHeaderMocker.createEnvelopeHeaderMock
import no.mattilsynet.ephyto.api.mocks.ephyto.EnvelopeMocker.createEnvelopeMock
import no.mattilsynet.ephyto.api.mocks.ephyto.NppoMocker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
internal class EphytoServiceTest {

    private lateinit var ephytoService: EphytoService

    @MockitoBean
    lateinit var envelopeService: EnvelopeService

    @MockitoBean
    lateinit var ephytoClient: EphytoClient

    @MockitoBean
    lateinit var ephytoKodeverkClient: EphytoKodeverkClient

    @MockitoBean
    lateinit var natsService: NatsService

    @MockitoBean
    lateinit var ephytoValideringService: EphytoValideringService

    private val xmlSertifikatContent = object {}.javaClass.classLoader
        .getResource("SPSCertificateContent.xml")!!.readText()

    @BeforeEach
    fun setUp() {
        ephytoService = EphytoService(
            envelopeService = envelopeService,
            ephytoClient = ephytoClient,
            ephytoKodeverkClient = ephytoKodeverkClient,
            ephytoValideringService = ephytoValideringService,
        )

        doReturn(ArrayOfNppo().also { arrayOfNppo ->
            arrayOfNppo.nppo.add(Nppo().also { nppo ->
                nppo.country = "DK"
            })
        }).`when`(ephytoKodeverkClient).getActiveNppos()

        doNothing().`when`(natsService).publishImportEnvelopeMetadata(any())
    }

    @Test
    fun `test av at hentNyeEnvelopes returnerer tom liste dersom ephytoclient returnerer null`() {
        // Given:
        doReturn(null).`when`(ephytoClient).getImportEnvelopeHeaders()

        // When:
        ephytoService.hentNyeEnvelopes(0)

        // Then:
        verify(envelopeService, times(0)).haandterNyEnvelope(any(), any())
    }

    @Test
    fun `test av at hentNyeEnvelopes haandterer envelopes dersom ephytoclient klarer aa hente envelopes`() {
        // Given:
        doReturn(listOf(createEnvelopeHeaderMock())).`when`(ephytoClient).getImportEnvelopeHeaders()
        doReturn(createEnvelopeMock(content = ""))
            .`when`(ephytoClient).getSingleImportEnvelopeOrNull(any())

        // When:
        val antallEnvelopes = ephytoService.hentNyeEnvelopes(0)

        // Then:
        verify(ephytoClient).getSingleImportEnvelopeOrNull(any())
        assertEquals(1, antallEnvelopes)
    }

    @Test
    fun `hentOgHaandterNyEnvelope kaller ack med null hvis ephytoclient returnerer null for envelope`() {
        // Given:
        doReturn(null).`when`(ephytoClient).getSingleImportEnvelopeOrNull(any())
        val mockValideringsresultat = createValideringsresultatMock(null)
        doReturn(mockValideringsresultat)
            .`when`(ephytoValideringService).getValidationResultForEnvelope(anyOrNull(), any())

        // When:
        ephytoService.hentOgHaandterNyEnvelope(createEnvelopeHeaderMock())

        // Then:
        verify(ephytoClient)
            .acknowledgeEnvelope(valideringsresultat = mockValideringsresultat)
    }

    @Test
    fun `hentOgHaandterNyEnvelope kaller haandterNyEnvelope dersom ephytoclient returnerer en envelope`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = xmlSertifikatContent)
        doReturn(envelopeMock).`when`(ephytoClient).getSingleImportEnvelopeOrNull(any())
        val valideringsresultatMock = createValideringsresultatMock(envelopeMock)
        doReturn(valideringsresultatMock)
            .`when`(ephytoValideringService).getValidationResultForEnvelope(any(), any())

        doReturn(valideringsresultatMock).`when`(envelopeService)
            .haandterNyEnvelope(envelopeMock, valideringsresultatMock)

        // When:
        ephytoService.hentOgHaandterNyEnvelope(createEnvelopeHeaderMock())

        // Then:
        verify(envelopeService).haandterNyEnvelope(
            envelope = envelopeMock,
            valideringsresultat = valideringsresultatMock,
        )
        verify(ephytoClient, times(1)).acknowledgeEnvelope(any())
    }

    @Test
    fun `hentOgHaandterNyEnvelope tar med valideringsresultat fra getValidationResultForEnvelope`() {
        // Given:
        val envelopeMock = createEnvelopeMock(content = null)
        doReturn(envelopeMock).`when`(ephytoClient).getSingleImportEnvelopeOrNull(any())
        val envelopeHeader = createEnvelopeHeaderMock()
        val valideringsresultatMock = createValideringsresultatMock(
            envelopeMock = envelopeMock,
            hubTrackingInfo = HUBTrackingInfo.DELIVERED_NOT_READABLE,
        )
        doReturn(valideringsresultatMock).`when`(ephytoValideringService)
            .getValidationResultForEnvelope(envelopeMock, envelopeMock.hubDeliveryNumber)

        doReturn(valideringsresultatMock).`when`(envelopeService)
            .haandterNyEnvelope(envelopeMock, valideringsresultatMock)

        // When:
        ephytoService.hentOgHaandterNyEnvelope(envelopeHeader)

        // Then:
        verify(envelopeService).haandterNyEnvelope(
            envelope = envelopeMock,
            valideringsresultat = valideringsresultatMock,
        )
        verify(ephytoClient, times(1))
            .acknowledgeEnvelope(valideringsresultatMock)
    }

    @Test
    fun `hentOgHaandterNyEnvelope kjoerer acknowledge med valideringsresultat naar envelope har valid content`() {
        // Given:
        val envelopeHeader = createEnvelopeHeaderMock()
        val envelopeMock = createEnvelopeMock(content = xmlSertifikatContent)

        doReturn(envelopeMock).`when`(ephytoClient).getSingleImportEnvelopeOrNull(any())
        val valideringsresultatMock = createValideringsresultatMock(envelopeMock)
        doReturn(valideringsresultatMock).`when`(ephytoValideringService)
            .getValidationResultForEnvelope(envelopeMock, envelopeMock.hubDeliveryNumber)

        doReturn(valideringsresultatMock).`when`(envelopeService)
            .haandterNyEnvelope(envelopeMock, valideringsresultatMock)

        // When:
        ephytoService.hentOgHaandterNyEnvelope(envelopeHeader)

        // Then:
        verify(envelopeService).haandterNyEnvelope(
            envelope = envelopeMock,
            valideringsresultat = valideringsresultatMock,
        )
        verify(ephytoClient, times(1))
            .acknowledgeEnvelope(valideringsresultat = valideringsresultatMock)
    }

    @Test
    fun `test hentOgHaandterNyEnvelope acknowledger ikke dersom hentOgHaandterNyEnvelope kaster feil`() {
        // Given:
        doThrow(IllegalArgumentException()).`when`(ephytoClient).getSingleImportEnvelopeOrNull(any())

        // When:
        ephytoService.hentOgHaandterNyEnvelope(createEnvelopeHeaderMock())

        // Then:
        verify(envelopeService, never()).haandterNyEnvelope(any(), any())
        verify(ephytoClient, never()).acknowledgeEnvelope(any())
    }

    @Test
    fun `hentNyeNppos returnerer tom liste dersom ephytoclient krasjer`() {
        // Given:
        doReturn(null).`when`(ephytoKodeverkClient).getActiveNppos()

        // When:
        val nppos = ephytoService.hentAktiveNppos()

        // Then:
        assertTrue(nppos.isEmpty())
    }

    @Test
    fun `hentNyeNppos returnerer tom liste dersom ephytoclient returnerer nppos som null`() {
        // Given:
        doReturn(null).`when`(ephytoKodeverkClient).getActiveNppos()

        // When:
        val nppos = ephytoService.hentAktiveNppos()

        // Then:
        assertTrue(nppos.isEmpty())
    }

    @Test
    fun `hentNyeNppos returnerer liste dersom ephytoclient klarer aa hente nppos`() {
        // Given:
        doReturn(ArrayOfNppo().also { arrayOfNppo ->
            arrayOfNppo.nppo.add(NppoMocker.createNppoMockUtenSignaturSertifikat())
        }).`when`(ephytoKodeverkClient).getActiveNppos()

        // When:
        val nppos = ephytoService.hentAktiveNppos()

        // Then:
        assertFalse(nppos.isEmpty())
    }

    @Test
    fun `hentStatements returnerer tom liste dersom ephytoclient returnerer null`() {
        // Given:
        doReturn(emptyList<Statement>()).`when`(ephytoKodeverkClient).getStatements()

        // When:
        val statements = ephytoService.hentStatements()

        // Then:
        assertTrue(statements.isEmpty())
    }

    @Test
    fun `hentStatements returnerer liste dersom ephytoclient klarer aa hente statements`() {
        // Given:
        doReturn(
            mutableListOf(
                Statement().also { statement ->
                    statement.code = "2"
                    statement.text = "They are deemed to be practically free from other pests."
                    statement.lang = "EN"
                },
                Statement().also { statement ->
                    statement.code = "2"
                    statement.text = "They are deemed to   be practic/nally free fr/rom other pests."
                    statement.lang = "en"
                },
                Statement().also { statement ->
                    statement.code = "2"
                    statement.text = "Denne teksten skal ikke med"
                    statement.lang = "no"
                },
            )
        ).`when`(ephytoKodeverkClient).getStatements()


        // When:
        val statements = ephytoService.hentStatements()

        // Then:
        assertTrue(statements.isNotEmpty())
        assertTrue(statements.size == 2)
        statements.map { it.text }.forEach { vasketStatement ->
            // Når verdien er vasket i hentStatements, så skal den være lik sin vaskede verdi
            assertEquals(vasketStatement, vasketStatement.vask())
        }
    }

    @Test
    fun `getIndendedUse returnerer tom liste dersom ephytoclient returnerer null`() {
        // Given:
        doReturn(emptyList<IntendedUse>()).`when`(ephytoKodeverkClient).getIndendedUse()

        // When:
        val intendedUses = ephytoService.hentIndendedUse()

        // Then:
        assertTrue(intendedUses.isEmpty())
    }

    @Test
    fun `getIndendedUse returnerer liste dersom ephytoclient klarer aa hente intendedUses`() {
        // Given:
        doReturn(
            listOf(
                IntendedUse().also { intendedUse ->
                    intendedUse.isActive = true
                    intendedUse.code = "0001"
                    intendedUse.name = "consumption"
                    intendedUse.lang = "EN"
                },
                IntendedUse().also { intendedUse ->
                    intendedUse.isActive = true
                    intendedUse.code = "0002"
                    intendedUse.name = "planting"
                    intendedUse.lang = "EN"
                },
                IntendedUse().also { intendedUse ->
                    intendedUse.isActive = true
                    intendedUse.code = "0003"
                    intendedUse.name = "decoration"
                    intendedUse.lang = "EN"
                },
            )
        ).`when`(ephytoKodeverkClient).getIndendedUse()


        // When:
        val intendedUses = ephytoService.hentIndendedUse()

        // Then:
        assertTrue(intendedUses.isNotEmpty())
        assertTrue(intendedUses.size == 3)
    }

    @Test
    fun `hentMeanOfTransports returnerer tom liste dersom ephytoclient returnerer null`() {
        // Given:
        doReturn(emptyList<MeanOfTransport>()).`when`(ephytoKodeverkClient).getMeanOfTransports()

        // When:
        val meanOfTransports = ephytoService.hentMeanOfTransports()

        // Then:
        assertTrue(meanOfTransports.isEmpty())
    }

    @Test
    fun `hentMeanOfTransports returnerer liste dersom ephytoclient klarer aa hente meanOfTransports`() {
        // Given:
        doReturn(
            listOf(
                MeanOfTransport().also { meanOfTransport ->
                    meanOfTransport.isActive = true
                    meanOfTransport.modeCode = 3
                    meanOfTransport.usedTransportMean = "Road transport"
                    meanOfTransport.lang = "EN"
                },
                MeanOfTransport().also { meanOfTransport ->
                    meanOfTransport.isActive = true
                    meanOfTransport.modeCode = 3
                    meanOfTransport.usedTransportMean = "R/roa  d tr/nansport"
                    meanOfTransport.lang = "en"
                },
                MeanOfTransport().also { meanOfTransport ->
                    meanOfTransport.isActive = true
                    meanOfTransport.modeCode = 3
                    meanOfTransport.usedTransportMean = "Denne teksten skal ikke med"
                    meanOfTransport.lang = "no"
                },
            )
        ).`when`(ephytoKodeverkClient).getMeanOfTransports()


        // When:
        val meanOfTransports = ephytoService.hentMeanOfTransports()

        // Then:
        assertTrue(meanOfTransports.isNotEmpty())
        assertTrue(meanOfTransports.isNotEmpty())
        assertTrue(meanOfTransports.size == 2)
        meanOfTransports.map { it.usedTransportMean }.forEach { vasketMeanOfTransport ->
            // Når verdien er vasket i hentMeanOfTransports, så skal den være lik sin vaskede verdi
            assertEquals(vasketMeanOfTransport, vasketMeanOfTransport.vask())
        }
    }

    @Test
    fun `hentTreatmentTypes returnerer tom liste dersom ephytoclient returnerer null`() {
        // Given:
        doReturn(emptyList<TreatmentType>()).`when`(ephytoKodeverkClient).getTreatmentTypes()

        // When:
        val treatmentTypes = ephytoService.hentTreatmentTypes()

        // Then:
        assertTrue(treatmentTypes.isEmpty())
    }

    @Test
    fun `hentTreatmentTypes returnerer liste dersom ephytoclient klarer aa hente treatmentTypes`() {
        // Given:
        doReturn(
            listOf(
                TreatmentType().also { treatmentType ->
                    treatmentType.isActive = true
                    treatmentType.code = "CHT"
                    treatmentType.description =
                        "Systems approach, Additional Information: No additional information available"
                    treatmentType.lang = "en"
                    treatmentType.level = 1
                },
                TreatmentType().also { treatmentType ->
                    treatmentType.isActive = true
                    treatmentType.code = "OP"
                    treatmentType.description =
                        "Systems approach, Additio/nnal Informat/rion: No a  dditional information available"
                    treatmentType.lang = "EN"
                    treatmentType.level = 2
                },
                TreatmentType().also { treatmentType ->
                    treatmentType.isActive = true
                    treatmentType.code = "OP"
                    treatmentType.description =
                        "Denne teksten skal ikke med"
                    treatmentType.lang = "no"
                    treatmentType.level = 2
                },
            )
        ).`when`(ephytoKodeverkClient).getTreatmentTypes()


        // When:
        val treatmentTypes = ephytoService.hentTreatmentTypes()

        // Then:
        assertTrue(treatmentTypes.isNotEmpty())
        assertTrue(treatmentTypes.size == 2)
        treatmentTypes.map { it.description }.forEach { vasketTreatmentType ->
            // Når verdien er vasket i hentTreatmentTypes, så skal den være lik sin vaskede verdi
            assertEquals(vasketTreatmentType, vasketTreatmentType.vask())
        }
    }
}
