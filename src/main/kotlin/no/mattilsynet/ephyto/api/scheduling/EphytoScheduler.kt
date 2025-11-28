package no.mattilsynet.ephyto.api.scheduling

import no.mattilsynet.ephyto.api.services.EphytoKodeverkService
import no.mattilsynet.ephyto.api.services.EphytoService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["ephyto.scheduler.enabled"], havingValue = "true", matchIfMissing = false)
class EphytoScheduler(
    private val ephytoKodeverkService: EphytoKodeverkService,
    private val ephytoService: EphytoService,
    @Value("\${ephyto.scheduler.envelopes.cron.sleeptime}") private val sleepTime: Long,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${ephyto.scheduler.envelopes.cron.expression}", zone = "\${ephyto.scheduler.zone}")
    fun getEnvelopes() {
        logger.info("Henter envelopes fra ePhyto og legger på kø")
        ephytoService.hentNyeEnvelopes(sleepTime).also { antall ->
            logger.info("Ferdig med å hente $antall envelopes fra ePhyto og legge på kø")
        }
    }

    @Scheduled(cron = "\${ephyto.scheduler.kodeverk.cron.expression}", zone = "\${ephyto.scheduler.zone}")
    fun getKodeverk() {
        logger.info("Henter kodeverk fra ephyto og legger på kø")
        ephytoKodeverkService.pushNpposTilNats()
        ephytoKodeverkService.pushIndendedUseTilNats()
        ephytoKodeverkService.pushConditionTilNats()
        ephytoKodeverkService.pushProductDescriptionTilNats()
        ephytoKodeverkService.pushUnitMeasureTilNats()
        ephytoKodeverkService.pushMeanOfTransportsTilNats()
        ephytoKodeverkService.pushStatementsTilNats()
        ephytoKodeverkService.pushTreatmentsTilNats()
        logger.info("Ferdig med å hente kodeverk fra ephyto og legge på kø")
    }

}
