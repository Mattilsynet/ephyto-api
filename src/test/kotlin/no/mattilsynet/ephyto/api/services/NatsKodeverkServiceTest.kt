package no.mattilsynet.ephyto.api.services

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import no.mattilsynet.ephyto.api.imports.kodeverk.v1.KodeverkDto
import no.mattilsynet.ephyto.api.imports.meanoftransport.v1.MeanOfTransportDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.NppoDto
import no.mattilsynet.ephyto.api.imports.statement.v1.StatementDto
import no.mattilsynet.ephyto.api.imports.treatmenttype.v1.TreatmentTypeDto
import no.mattilsynet.ephyto.api.imports.unitmeasure.v1.UnitMeasureDto
import no.mattilsynet.ephyto.api.mocks.ephyto.ConditionMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.IntendedUseMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.MeanOfTransportMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.NppoMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.ProductDescriptionMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.StatementMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.TreatmentTypeMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.UnitMeasureMocker
import no.mattilsynet.fisk.libs.springtest.SpringVirtualNatsTestStarter
import no.mattilsynet.fisk.libs.virtualnats.VirtualNats
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@Import(SpringVirtualNatsTestStarter::class)
@Suppress("UnusedPrivateProperty")
internal class NatsKodeverkServiceTest {

    private lateinit var natsKodeverkService: NatsKodeverkService

    @Autowired
    private lateinit var reactiveNats: VirtualNats

    @MockitoBean
    private lateinit var secretManagerServiceClient: SecretManagerServiceClient

    @MockitoBean
    private lateinit var gcpStorageService: GcpStorageService

    @MockitoBean
    private lateinit var envelopeService: EnvelopeService

    @BeforeEach
    fun setUp() {
        natsKodeverkService = NatsKodeverkService(reactiveNats)
    }

    @AfterEach
    fun cleanUp() {
        reactiveNats.keyValue("ephyto_import_active_nppos_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_condition_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_intended_use_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_product_description_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_statements_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_transport_methods_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_treatment_types_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_unit_measure_v1").purgeAll()
    }

    @Test
    fun `putNppos kjoerer uten problemer med en liste med nppoer`() {
        // Given:
        val nppos = listOf(
            NppoMocker.createNppoMedAlleData(countryCode = "NL"),
        )

        // When:
        natsKodeverkService.putNppos(nppos)

        // Then:
        assertEquals(
            "NL",
            NppoDto.parseFrom(
                getFraNats(bucket = "ephyto_import_active_nppos_v1", key = "NL")
            ).country,
        )
    }

    @Test
    fun `putStatements kjoerer uten problemer med et statement`() {
        // Given:
        val statements = listOf(
            StatementMocker.createStatementMock()
        )

        // When:
        natsKodeverkService.putStatements(statements)

        // Then:
        assertEquals(
            "text",
            StatementDto.parseFrom(
                getFraNats(bucket = "ephyto_import_statements_v1", key = "code/DK")
            ).text,
        )
    }

    @Test
    fun `putIntendedUse kjoerer uten problemer med riktig bucket`() {
        // Given:
        val intendedUses = listOf(
            IntendedUseMocker.createIntendedUseMock(lang = "en"),
            IntendedUseMocker.createIntendedUseMock(lang = "es"),
            IntendedUseMocker.createIntendedUseMock(lang = "fr"),
            IntendedUseMocker.createIntendedUseMock(lang = "ukjent"),
        )

        // When:
        natsKodeverkService.putIntendedUse(intendedUses)

        // Then:
        with(
            KodeverkDto.parseFrom(
                getFraNats(bucket = "ephyto_import_intended_use_v1", key = "code")
            )
        ) {
            assertEquals("name - en", beskrivelseEn)
            assertEquals("name - es", beskrivelseEs)
            assertEquals("name - fr", beskrivelseFr)
        }
    }

    @Test
    fun `putCondition kjoerer uten problemer med riktig bucket`() {
        // Given:
        val conditions = listOf(
            ConditionMocker.createConditionMock(lang = "en"),
            ConditionMocker.createConditionMock(lang = "es"),
            ConditionMocker.createConditionMock(lang = "fr"),
            ConditionMocker.createConditionMock(lang = "ukjent"),
        )

        // When:
        natsKodeverkService.putCondition(conditions)

        // Then:
        with(
            KodeverkDto.parseFrom(
                getFraNats(bucket = "ephyto_import_condition_v1", key = "code")
            )
        ) {
            assertEquals("name - en", beskrivelseEn)
            assertEquals("name - es", beskrivelseEs)
            assertEquals("name - fr", beskrivelseFr)
        }
    }

    @Test
    fun `putProductDescription kjoerer uten problemer med riktig bucket`() {
        // Given:
        val productDescriptions = listOf(
            ProductDescriptionMocker.createProductDescriptionMock(lang = "en"),
            ProductDescriptionMocker.createProductDescriptionMock(lang = "es"),
            ProductDescriptionMocker.createProductDescriptionMock(lang = "fr"),
            ProductDescriptionMocker.createProductDescriptionMock(lang = "ukjent"),
        )

        // When:
        natsKodeverkService.putProductDescription(productDescriptions)

        // Then:
        with(
            KodeverkDto.parseFrom(
                getFraNats(bucket = "ephyto_import_product_description_v1", key = "code")
            )
        ) {
            assertEquals("name - en", beskrivelseEn)
            assertEquals("name - es", beskrivelseEs)
            assertEquals("name - fr", beskrivelseFr)
        }
    }

    @Test
    fun `putUnitMeasure kjoerer uten problemer med riktig bucket`() {
        // Given:
        val unitMeasures = listOf(
            UnitMeasureMocker.createUnitMeasureMock(),
        )

        // When:
        natsKodeverkService.putUnitMeasure(unitMeasures)

        // Then:
        with(
            UnitMeasureDto.parseFrom(
                getFraNats(bucket = "ephyto_import_unit_measure_v1", key = "code")
            )
        ) {
            assertEquals("kg/m", beskrivelse)
        }
    }

    @Test
    fun `putTreatmentTypes kjoerer uten problemer med en treatmentType`() {
        // Given:
        val treatmentTypes = listOf(
            TreatmentTypeMocker.createTreatmentType()
        )

        // When:
        natsKodeverkService.putTreatmentTypes(treatmentTypes)

        // Then:
        assertEquals(
            "description",
            TreatmentTypeDto.parseFrom(
                getFraNats(bucket = "ephyto_import_treatment_types_v1", key = "code/lang")
            ).description,
        )
    }

    @Test
    fun `putMeanOfTransports kjoerer uten problemer med en transporttype`() {
        // Given:
        val meanOfTransports = listOf(
            MeanOfTransportMocker.createMeanOfTransportMock()
        )

        // When:
        natsKodeverkService.putMeanOfTransports(meanOfTransports)

        // Then:
        assertEquals(
            "Vessel",
            MeanOfTransportDto.parseFrom(
                getFraNats(bucket = "ephyto_import_transport_methods_v1", key = "1/en")
            ).usedTransportMean,
        )
    }

    private fun getFraNats(
        bucket: String,
        key: String,
    ) = reactiveNats.keyValue(bucket)
        .get(key = key)?.let { eppokode ->
            runCatching { eppokode.getValue() }.getOrNull()
        }

}
