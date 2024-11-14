package no.mattilsynet.ephyto.api.services

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import io.nats.client.KeyValueOptions
import no.mattilsynet.ephyto.api.imports.intendeduse.v1.IntendedUseDto
import no.mattilsynet.ephyto.api.imports.meanoftransport.v1.MeanOfTransportDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.NppoDto
import no.mattilsynet.ephyto.api.imports.statement.v1.StatementDto
import no.mattilsynet.ephyto.api.imports.treatmenttype.v1.TreatmentTypeDto
import no.mattilsynet.ephyto.api.mocks.ephyto.IntendedUseMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.MeanOfTransportMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.NppoMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.StatementMocker
import no.mattilsynet.ephyto.api.mocks.ephyto.TreatmentTypeMocker
import no.mattilsynet.fisk.libs.reactivenats.ReactiveNats
import no.mattilsynet.fisk.libs.springnatstest.spring.SpringNatsTestConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@MockBean(
    EnvelopeService::class,
    GcpStorageService::class,
    SecretManagerServiceClient::class,
)
@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@Import(SpringNatsTestConfiguration::class)
class NatsKodeverkServiceTest {

    private lateinit var natsKodeverkService: NatsKodeverkService

    @Autowired
    private lateinit var reactiveNats: ReactiveNats

    @BeforeEach
    fun setUp() {
        natsKodeverkService = NatsKodeverkService(reactiveNats)
    }

    @AfterEach
    fun cleanUp() {
        reactiveNats.keyValue("ephyto_import_active_nppos_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_statements_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_treatment_types_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_transport_methods_v1").purgeAll()
        reactiveNats.keyValue("ephyto_import_intended_use_v1").purgeAll()
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
            NppoDto.parseFrom(
                getFraNats(bucket = "ephyto_import_active_nppos_v1", key = "NL")
            ).country,
            "NL",
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
            StatementDto.parseFrom(
                getFraNats(bucket = "ephyto_import_statements_v1", key = "code/DK")
            ).text,
            "text",
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
            IntendedUseDto.parseFrom(
                getFraNats(bucket = "ephyto_import_intended_use_v1", key = "code")
            )
        ) {
            assertEquals(beskrivelseEn, "name - en")
            assertEquals(beskrivelseEs, "name - es")
            assertEquals(beskrivelseFr, "name - fr")
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
            TreatmentTypeDto.parseFrom(
                getFraNats(bucket = "ephyto_import_treatment_types_v1", key = "code/lang")
            ).description,
            "description",
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
            MeanOfTransportDto.parseFrom(
                getFraNats(bucket = "ephyto_import_transport_methods_v1", key = "1/en")
            ).usedTransportMean,
            "Vessel",
        )
    }

    private fun getFraNats(
        bucket: String,
        key: String,
    ) =
        reactiveNats.keyValue(
            bucket,
            KeyValueOptions.builder().build()
        ).get(key = key)
            .mapNotNull { eppokode ->
                runCatching {
                    eppokode.getValue()
                }.getOrNull()
            }
            .toFuture()
            .get()

}
