package no.mattilsynet.ephyto.api.logic

import _int.ippc.ephyto.MeanOfTransport
import _int.ippc.ephyto.Statement
import _int.ippc.ephyto.TreatmentType
import _int.ippc.ephyto.UnitMeasure

fun String.vask() =
    replace("\n", " ")
        .replace("\r", " ")
        .replace("\t", " ")
        .replace("\\s+".toRegex(), " ")
        .trim()
        .let {
            when {
                it.length == "NULL".length -> {
                    it.replace("NULL", "", true)
                }

                "[\\W+ ]+".toRegex().matches(it) -> "" // Returnerer tom string dersom det kun er tegn i strengen
                else -> {
                    it
                }
            }
        }

fun List<Statement>.vaskStatements() =
    onEach { statement ->
        statement.text = statement.text.vask()
    }

fun List<MeanOfTransport>.vaskMeanOfTransports() =
    onEach { meanOfTransport ->
       meanOfTransport.usedTransportMean = meanOfTransport.usedTransportMean.vask()
    }

fun List<TreatmentType>.vaskTreatmentType() =
    onEach { treatmentType ->
        treatmentType.description = treatmentType.description.vask()
    }

fun List<UnitMeasure>.vaskUnitMeasure() =
    onEach { unitMeasure ->
        unitMeasure.symbol = unitMeasure.symbol
            .replace("\n", "")
            .replace("\r", "")
    }
