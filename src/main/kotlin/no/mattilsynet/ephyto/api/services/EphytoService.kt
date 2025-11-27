package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.Condition
import _int.ippc.ephyto.IntendedUse
import _int.ippc.ephyto.MeanOfTransport
import _int.ippc.ephyto.ProductDescription
import _int.ippc.ephyto.Statement
import _int.ippc.ephyto.TreatmentType
import _int.ippc.ephyto.UnitMeasure
import _int.ippc.ephyto.hub.EnvelopeHeader
import _int.ippc.ephyto.hub.Nppo
import no.mattilsynet.ephyto.api.clients.EphytoClient
import no.mattilsynet.ephyto.api.clients.EphytoKodeverkClient
import no.mattilsynet.ephyto.api.logic.vaskMeanOfTransports
import no.mattilsynet.ephyto.api.logic.vaskStatements
import no.mattilsynet.ephyto.api.logic.vaskTreatmentType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EphytoService(
    private val ephytoValideringService: EphytoValideringService,
    private val ephytoClient: EphytoClient,
    private val ephytoKodeverkClient: EphytoKodeverkClient,
    private val envelopeService: EnvelopeService,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun hentNyeEnvelopes(sleepTime: Long) =
        ephytoClient.getImportEnvelopeHeaders()
            ?.also { Thread.sleep(sleepTime) }
            ?.onEach { envelopeHeader ->
            hentOgHaandterNyEnvelope(envelopeHeader)
        }?.size

    fun hentOgHaandterNyEnvelope(envelopeHeader: EnvelopeHeader) {
        runCatching {
            ephytoClient.getSingleImportEnvelopeOrNull(envelopeHeader.hubDeliveryNumber).also { envelope ->
                var valideringsresultat = ephytoValideringService
                    .getValidationResultForEnvelope(envelope, envelopeHeader.hubDeliveryNumber)
                if (envelope != null) {
                    valideringsresultat = envelopeService.haandterNyEnvelope(envelope, valideringsresultat)
                } else {
                    logger.error("Kunne ikke hente envelope med hubLeveringNummer ${envelopeHeader.hubDeliveryNumber}")
                }

                ephytoClient.acknowledgeEnvelope(valideringsresultat)
            }
        }.onFailure {
            logger
                .error("Håndtering av envelope med hubLeveringNummer ${envelopeHeader.hubDeliveryNumber} feilet," +
                        " ${ it.message }", it)
        }
    }

    fun hentAktiveNppos(): List<Nppo> =
        ephytoKodeverkClient.getActiveNppos()?.nppo ?: emptyList()

    fun hentStatements(): List<Statement> =
        ephytoKodeverkClient.getStatements()
            .filter { it.lang.equals(other = "en", ignoreCase = true) }
            .vaskStatements()

    fun hentTreatmentTypes(): List<TreatmentType> =
        ephytoKodeverkClient.getTreatmentTypes()
            .filter { it.lang.equals(other = "en", ignoreCase = true) }
            .vaskTreatmentType()

    fun hentMeanOfTransports(): List<MeanOfTransport> =
        ephytoKodeverkClient.getMeanOfTransports()
            .filter { it.lang.equals(other = "en", ignoreCase = true) }
            .vaskMeanOfTransports()

    fun hentIndendedUse(): List<IntendedUse> =
        ephytoKodeverkClient.getIndendedUse()

    fun hentUnitMeasures(): List<UnitMeasure> =
        ephytoKodeverkClient.getUnitMeasures()

    fun hentConditions(): List<Condition> =
        ephytoKodeverkClient.getConditions()

    fun hentProductDescriptions(): List<ProductDescription> =
        ephytoKodeverkClient.getProductDescriptions()
}
