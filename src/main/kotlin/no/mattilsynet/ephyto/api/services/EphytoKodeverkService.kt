package no.mattilsynet.ephyto.api.services

import org.springframework.stereotype.Service

@Service
class EphytoKodeverkService(
    private val ephytoService: EphytoService,
    private val natsKodeverkService: NatsKodeverkService,
) {

    fun pushNpposTilNats() =
        natsKodeverkService.putNppos(
            nppos = ephytoService.hentAktiveNppos()
        )

    fun pushStatementsTilNats() =
        natsKodeverkService.putStatements(
            statements = ephytoService.hentStatements()
        )

    fun pushMeanOfTransportsTilNats() =
        natsKodeverkService.putMeanOfTransports(
            meanOfTransports = ephytoService.hentMeanOfTransports()
        )

    fun pushIndendedUseTilNats() =
        natsKodeverkService.putIntendedUse(
            indendedUse = ephytoService.hentIndendedUse()
        )

    fun pushUnitMeasureTilNats() =
        natsKodeverkService.putUnitMeasure(
            unitMeasures = ephytoService.hentUnitMeasure()
        )

    fun pushConditionTilNats() =
        natsKodeverkService.putCondition(
            condition = ephytoService.hentCondition()
        )

    fun pushProductDescriptionTilNats() =
        natsKodeverkService.putProductDescription(
            productDescription = ephytoService.hentProductDescription()
        )

    fun pushTreatmentsTilNats() =
        natsKodeverkService.putTreatmentTypes(
            treatmentTypes = ephytoService.hentTreatmentTypes()
        )
}
