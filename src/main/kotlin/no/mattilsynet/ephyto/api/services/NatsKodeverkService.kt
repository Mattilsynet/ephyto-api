package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.IntendedUse
import _int.ippc.ephyto.MeanOfTransport
import _int.ippc.ephyto.Statement
import _int.ippc.ephyto.TreatmentType
import _int.ippc.ephyto.hub.Nppo
import io.nats.client.KeyValueOptions
import no.mattilsynet.ephyto.api.extensions.toTimestamp
import no.mattilsynet.ephyto.api.imports.intendeduse.v1.IntendedUseDto
import no.mattilsynet.ephyto.api.imports.meanoftransport.v1.MeanOfTransportDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.AllowedDocumentDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.ArrayOfChannelRulesDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.CertificateStatusDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.CertificateTypeDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.ChannelDirection
import no.mattilsynet.ephyto.api.imports.nppo.v1.ChannelRuleDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.ChannelRuleType
import no.mattilsynet.ephyto.api.imports.nppo.v1.NppoDto
import no.mattilsynet.ephyto.api.imports.nppo.v1.SigningCertificateDto
import no.mattilsynet.ephyto.api.imports.statement.v1.StatementDto
import no.mattilsynet.ephyto.api.imports.treatmenttype.v1.TreatmentTypeDto
import no.mattilsynet.fisk.libs.reactivenats.ReactiveNats
import org.springframework.stereotype.Service
import org.threeten.bp.Instant

@Service
@Suppress("MagicNumber")
class NatsKodeverkService(
    private val reactiveNats: ReactiveNats,
) {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    fun putNppos(nppos: List<Nppo>) {
        runCatching {
            nppos.forEach { nppo ->
                reactiveNats.keyValue(
                    "ephyto_import_active_nppos_v1",
                    KeyValueOptions.builder().build()
                ).put(
                    key = nppo.country,
                    value = nppoToProto(nppo).toByteArray(),
                ).subscribe()
            }
        }.onFailure {
            logger.warn("putNppos feilet med meldingen ${ it.message }", it)
        }
    }

    fun putStatements(statements: List<Statement>) {
        runCatching {
            statements.forEach { statement ->
                reactiveNats.keyValue(
                    "ephyto_import_statements_v1",
                    KeyValueOptions.builder().build()
                ).put(
                    key = "${statement.code}/${statement.lang}",
                    value = statementToProto(statement).toByteArray(),
                ).subscribe()
            }
        }.onFailure {
            logger.warn("putStatements feilet med meldingen ${ it.message }", it)
        }
    }

    private fun statementToProto(statement: Statement): StatementDto =
        with(statement) {
            StatementDto.newBuilder()
                .setCode(code)
                .setDocTypeOnly(docTypeOnly)
                .setLang(lang)
                .also { statementDtoBuilder ->
                    lastModified?.let { lastModified ->
                        statementDtoBuilder.setLastModified(lastModified.toTimestamp())
                    }
                }
                .setText(text)
                .setReceivedAt(Instant.now().toTimestamp())
                .build()
        }

    fun putIntendedUse(indendedUse: List<IntendedUse>) {
        runCatching {
            indendedUse
                .filter { it.lang == "en" || it.lang == "es" || it.lang == "fr" }
                .groupBy { it.code }
                .map { intendedUse ->
                    reactiveNats.keyValue(
                        "ephyto_import_intended_use_v1",
                        KeyValueOptions.builder().build()
                    ).put(
                        key = intendedUse.key,
                        value = intendedUseToProto(
                            intendedUseKode = intendedUse.key,
                            intendedUseBeskrivelser = intendedUse.value,
                        ).toByteArray(),
                    ).subscribe()
                }
        }.onFailure {
            logger.warn("putIntendedUse feilet med meldingen ${ it.message }", it)
        }
    }

    private fun intendedUseToProto(intendedUseKode: String, intendedUseBeskrivelser: List<IntendedUse>)
    : IntendedUseDto =
            IntendedUseDto.newBuilder()
                .setBeskrivelseEn(intendedUseBeskrivelser.first { it.lang == "en" }.name)
                .setBeskrivelseEs(intendedUseBeskrivelser.first { it.lang == "es" }.name)
                .setBeskrivelseFr(intendedUseBeskrivelser.first { it.lang == "fr" }.name)
                .setReceivedAt(Instant.now().toTimestamp())
                .setKode(intendedUseKode)
                .build()

    fun putMeanOfTransports(meanOfTransports: List<MeanOfTransport>) {
        runCatching {
            meanOfTransports.forEach { meanOfTransport ->
                reactiveNats.keyValue(
                    "ephyto_import_transport_methods_v1",
                    KeyValueOptions.builder().build()
                ).put(
                    key = "${meanOfTransport.modeCode}/${meanOfTransport.lang}",
                    value = meanOfTransportToProto(meanOfTransport).toByteArray(),
                ).subscribe()
            }
        }.onFailure {
            logger.warn("putMeanOfTransports feilet med meldingen ${ it.message }", it)
        }
    }

    private fun meanOfTransportToProto(meanOfTransport: MeanOfTransport): MeanOfTransportDto =
        with(meanOfTransport) {
            MeanOfTransportDto.newBuilder()
                .setLang(lang)
                .also { meanOfTransportsDtoBuilder ->
                    lastModified?.let { lastModified ->
                        meanOfTransportsDtoBuilder.setLastModified(lastModified.toTimestamp())
                    }
                }
                .setModeCode(modeCode)
                .setUsedTransportMean(usedTransportMean)
                .setReceivedAt(Instant.now().toTimestamp())
                .build()
        }

    fun putTreatmentTypes(treatmentTypes: List<TreatmentType>) {
        runCatching {
            treatmentTypes.forEach { treatmentType ->
                reactiveNats.keyValue(
                    "ephyto_import_treatment_types_v1",
                    KeyValueOptions.builder().build()
                ).put(
                    key = "${treatmentType.code}/${treatmentType.lang}",
                    value = treatmentTypeToProto(treatmentType).toByteArray(),
                ).subscribe()
            }
        }.onFailure {
            logger.warn("putTreatmentTypes feilet med meldingen ${ it.message }", it)
        }
    }

    private fun treatmentTypeToProto(treatmentType: TreatmentType): TreatmentTypeDto =
        with(treatmentType) {
            TreatmentTypeDto.newBuilder()
                .setCode(code)
                .setDescription(description)
                .setLang(lang)
                .also { treatmentTypeDtoBuilder ->
                    lastModified?.let { lastModified ->
                        treatmentTypeDtoBuilder.setLastModified(lastModified.toTimestamp())
                    }
                }
                .setLevel(level)
                .also { treatmentTypeDtoBuilder ->
                    parentCode?.let { parentCode ->
                        treatmentTypeDtoBuilder.setParentCode(parentCode)
                    }
                }
                .setReceivedAt(Instant.now().toTimestamp())
                .build()
        }

    @Suppress("LongMethod")
    private fun nppoToProto(nppo: Nppo): NppoDto =
        with(nppo) {
            NppoDto.newBuilder()
                .setCountry(country)
                .setSend(send)
                .setReceive(receive)
                .addAllAllowedDocument(
                    allowedDocument.mapNotNull { allowedDocument ->
                        AllowedDocumentDto.newBuilder()
                            .setActive(allowedDocument.isActive)
                            .setCertificateStatus(
                                CertificateStatusDto.newBuilder()
                                    .setValue(allowedDocument.certificateStatus.value)
                                    .setNumber(allowedDocument.certificateStatus.number)
                            )
                            .setCertificateType(
                                CertificateTypeDto.newBuilder()
                                    .setValue(allowedDocument.certificateType.value)
                                    .setNumber(allowedDocument.certificateType.number)
                            )
                            .build()
                    }
                )
                .setReceivedAt(Instant.now().toTimestamp())
                .also { builder ->

                    channelRules?.let { arrayOfChannelRulesDto ->
                        builder.setChannelRules(
                            ArrayOfChannelRulesDto.newBuilder()
                                .addAllChannelRule(
                                    arrayOfChannelRulesDto.channelRule.mapNotNull { channelRule ->
                                        ChannelRuleDto.newBuilder()
                                            .setActive(channelRule.isActive)
                                            .setId(channelRule.id)
                                            .setDirection(ChannelDirection.valueOf(channelRule.direction.value()))
                                            .setRuleType(ChannelRuleType.valueOf(channelRule.ruleType.value()))
                                            .setCountryCode(channelRule.countryCode)
                                            .setCertificateStatus(channelRule.certificateStatus)
                                            .setCertificateType(channelRule.certificateType)
                                            .build()
                                    }
                                )
                        )
                    }

                    signature?.let {
                        builder.setSignature(
                            SigningCertificateDto.newBuilder()
                                .setCertificate(it.certificate)
                                .setDn(it.dn)
                                .build()
                        )
                    }
                }
                .build()
        }

}
