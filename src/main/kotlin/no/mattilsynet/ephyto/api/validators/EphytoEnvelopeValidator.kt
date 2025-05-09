package no.mattilsynet.ephyto.api.validators

import _int.ippc.ephyto.ValidationLevel
import _int.ippc.ephyto.ValidationResult
import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.HUBTrackingInfo
import no.mattilsynet.ephyto.api.clients.EphytoClient
import no.mattilsynet.ephyto.api.domain.Valideringsresultat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

@Component
class EphytoEnvelopeValidator(
    private val ephytoClient: EphytoClient,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getValideringsresultat(envelope: Envelope): Valideringsresultat =
        run {
            val dekodetInnhold = parseEnvelopeContent(envelope = envelope)
            getAlvorligeEnvelopeValidationResultsFraEphyto(dekodetInnhold)
                ?.let { alvorligeValideringFeil ->
                    logger.warn(
                        "Alvorlige valideringsfeil ved ephytos validering av sertifikat med hubLeveringNummer " +
                                "${envelope.hubDeliveryNumber}, Feilmeldinger: $alvorligeValideringFeil"
                    )
                    return Valideringsresultat(
                        envelope = envelope,
                        errorMessage = alvorligeValideringFeil,
                        hubLeveringNummer = envelope.hubDeliveryNumber,
                        hubTrackingInfo = HUBTrackingInfo.DELIVERED_WITH_WARNINGS,
                        validatedOk = true,
                    )
                }
            if (validerEnvelopeContentLesbarhet(dekodetInnhold)) {
                return Valideringsresultat(
                    envelope = envelope,
                    errorMessage = null,
                    hubLeveringNummer = envelope.hubDeliveryNumber,
                    hubTrackingInfo = HUBTrackingInfo.DELIVERED,
                    validatedOk = true,
                )
            }

            return Valideringsresultat(
                envelope = envelope,
                errorMessage = "The XML content in the envelope is not readable",
                hubLeveringNummer = envelope.hubDeliveryNumber,
                hubTrackingInfo = HUBTrackingInfo.DELIVERED_NOT_READABLE,
                validatedOk = false,
            )
        }

    private fun parseEnvelopeContent(envelope: Envelope): String =
        runCatching {
            String(Base64.getDecoder().decode(envelope.content))
        }.getOrElse {
            envelope.content
        }

    private fun getAlvorligeEnvelopeValidationResultsFraEphyto(content: String): String? =
        ephytoClient.validatePhytoXml(content).filter {
            it.level == ValidationLevel.SEVERE
        }.takeIf { alvorligeValideringsfeil ->
            alvorligeValideringsfeil.isNotEmpty()
        }?.toFeilmelding()

    private fun validerEnvelopeContentLesbarhet(content: String): Boolean =
        runCatching {
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(
                    content.byteInputStream(StandardCharsets.UTF_8)
                )
        }.onFailure { exception ->
            logger.warn("XML-dokumentet er ikke leselig, Feilmelding: ${exception.message}", exception)
        }.isSuccess
}

fun List<ValidationResult>.toFeilmelding(): String =
    joinToString(separator = " ; ") { validationResult ->
        "Valideringsfeil={ " +
                "level='${validationResult.level}', " +
                "field='${validationResult.field}', " +
                "area={ name='${validationResult.area.name}', value='${validationResult.area.value()}' }, " +
                "message='${validationResult.msg}')" +
                "}"
    }

