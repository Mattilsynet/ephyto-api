package no.mattilsynet.ephyto.api.controllers

import io.swagger.v3.oas.annotations.Parameter
import no.mattilsynet.ephyto.api.controllers.mock.CreateEphytoMockdataService
import no.mattilsynet.ephyto.api.services.EphytoService
import no.mattilsynet.ephyto.api.services.NatsKodeverkService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("test", "local", "dev")
@RequestMapping("ephyto/v1")
class EphytoController(
    private val createEphytoMockdataService: CreateEphytoMockdataService,
    private val ephytoService: EphytoService,
    private val natsKodeverkService: NatsKodeverkService,
    ) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // TOD O: For testing
    @Profile("test", "local")
    @PostMapping("/sertifikater")
    fun ephyto(): ResponseEntity<Unit> =
        runCatching {
            ephytoService.hentNyeEnvelopes(0).let {
                logger.info("Har hentet $it sertifikater fra hub'en")
                return ResponseEntity.noContent().build()
            }
        }.onFailure {
            throw it
        }.getOrDefault(ResponseEntity.noContent().build())

    @Profile("test", "local")
    @PostMapping("/send/envelope")
    fun sendEphytoEnvelope(

        @Parameter(
            name = "antall",
            description = "Antall sertifikater som skal lages. 1 hvis det ikke sendes inn noe",
        )
        @RequestParam antall: Int?,
        @Parameter(
            name = "erstatterSertifikatNummer",
            description = "Legg inn sertifikatnummer til sertifikatet som skal erstattes",
        )
        @RequestParam erstatterSertifikatNummer: String?,

        @Parameter(
            name = "status",
            description = "Kodene kommer fra https://www.ephytoexchange.org/doc/mapping/IPPC_Specific_Status_Codes.pdf",
        )
        @RequestParam status: Int?,

        @Parameter(
            name = "type",
            description = "Eksport = 851, reeksport = 657",
        )
        @RequestParam type: Int?,
    ): ResponseEntity<Unit> =
        runCatching {
            createEphytoMockdataService.sendEphytoEnvelope(
                antall = antall ?: 1,
                erstatterSertifikatNummer = erstatterSertifikatNummer,
                status = status,
                type = type,
            ).let {
                logger.info("Ferdig med å sende sertifikater til hub'en")
                return ResponseEntity.noContent().build()
            }
        }.onFailure {
            throw it
        }.getOrDefault(ResponseEntity.noContent().build())

    @Profile("test", "local")
    @PostMapping("/nats/push/nppo")
    fun pushNpposTilNats(): ResponseEntity<String> =
        natsKodeverkService.putNppos(
            nppos = ephytoService.hentAktiveNppos()
        ).let {
            ResponseEntity.noContent().build()
        }

    @Profile("test", "local")
    @PostMapping("/nats/push/statements")
    fun pushStatementsTilNats(): ResponseEntity<String> =
        natsKodeverkService.putStatements(
            statements = ephytoService.hentStatements()
        ).let {
            ResponseEntity.noContent().build()
        }

    @Profile("test", "local")
    @PostMapping("/nats/push/intendeduse")
    fun pushIntendedUseTilNats(): ResponseEntity<String> =
        natsKodeverkService.putIntendedUse(
            ephytoService.hentIndendedUse()
        ).let {
            ResponseEntity.noContent().build()
        }

    @Profile("test", "local")
    @PostMapping("/nats/push/meanoftransports")
    fun pushMeanOfTransportsTilNats(): ResponseEntity<String> =
        natsKodeverkService.putMeanOfTransports(
            meanOfTransports = ephytoService.hentMeanOfTransports()
        ).let {
            ResponseEntity.noContent().build()
        }

    @Profile("test", "local")
    @PostMapping("/nats/push/treatmenttypes")
    fun pushTreatmentsTilNats(): ResponseEntity<String> =
        natsKodeverkService.putTreatmentTypes(
            treatmentTypes = ephytoService.hentTreatmentTypes()
        ).let {
            ResponseEntity.noContent().build()
        }

}
