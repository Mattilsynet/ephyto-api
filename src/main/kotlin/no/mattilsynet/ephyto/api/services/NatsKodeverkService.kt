package no.mattilsynet.ephyto.api.services

import _int.ippc.ephyto.Condition
import _int.ippc.ephyto.IntendedUse
import _int.ippc.ephyto.MeanOfTransport
import _int.ippc.ephyto.ProductDescription
import _int.ippc.ephyto.Statement
import _int.ippc.ephyto.TreatmentType
import _int.ippc.ephyto.UnitMeasure
import _int.ippc.ephyto.hub.Nppo
import no.mattilsynet.ephyto.api.extensions.toTimestamp
import no.mattilsynet.ephyto.api.imports.kodeverk.v1.KodeverkDto
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
import no.mattilsynet.ephyto.api.imports.unitmeasure.v1.UnitMeasureDto
import no.mattilsynet.ephyto.api.logic.vaskUnitMeasure
import no.mattilsynet.virtualnats.virtualnatscore.VirtualNats
import org.springframework.stereotype.Service
import org.threeten.bp.Instant

@Service
@Suppress("MagicNumber", "TooManyFunctions")
class NatsKodeverkService(private val nats: VirtualNats) {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    fun putNppos(nppos: List<Nppo>) {

        runCatching {
            nppos.forEach { nppo ->
                nats.keyValue(
                    "ephyto_import_active_nppos_v1"
                ).put(
                    key = nppo.country,
                    value = nppoToProto(nppo).toByteArray(),
                )
            }
        }.onFailure {
            logger.warn("putNppos feilet med meldingen ${it.message}", it)
        }
    }

    fun putStatements(statements: List<Statement>) {
        runCatching {
            statements.forEach { statement ->
                nats.keyValue(
                    "ephyto_import_statements_v1"
                ).put(
                    key = "${statement.code}/${statement.lang}",
                    value = statementToProto(statement).toByteArray(),
                )
            }
        }.onFailure {
            logger.warn("putStatements feilet med meldingen ${it.message}", it)
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
                    nats.keyValue(
                        "ephyto_import_intended_use_v1"
                    ).put(
                        key = intendedUse.key,
                        value = kodeverkToProto(
                            kode = intendedUse.key,
                            beskrivelseEn = intendedUse.value.first { it.lang == "en" }.name,
                            beskrivelseEs = intendedUse.value.first { it.lang == "es" }.name,
                            beskrivelseFr = intendedUse.value.first { it.lang == "fr" }.name,
                        ).toByteArray(),
                    )
                }
        }.onFailure {
            logger.warn("putIntendedUse feilet med meldingen ${it.message}", it)
        }
    }

    fun putCondition(condition: List<Condition>) {
        runCatching {
            condition
                .filter { it.lang == "en" || it.lang == "es" || it.lang == "fr" }
                .groupBy { it.code }
                .map { condition ->
                    nats.keyValue(
                        "ephyto_import_condition_v1"
                    ).put(
                        key = condition.key,
                        value = kodeverkToProto(
                            kode = condition.key,
                            beskrivelseEn = condition.value.first { it.lang == "en" }.name,
                            beskrivelseEs = condition.value.first { it.lang == "es" }.name,
                            beskrivelseFr = condition.value.first { it.lang == "fr" }.name,
                        ).toByteArray(),
                    )
                }
        }.onFailure {
            logger.warn("putCondition feilet med meldingen ${it.message}", it)
        }
    }

    fun putProductDescription(productDescription: List<ProductDescription>) {
        runCatching {
            productDescription
                .filter { it.lang == "en" || it.lang == "es" || it.lang == "fr" }
                .groupBy { it.code }
                .map { productDescription ->
                    nats.keyValue(
                        "ephyto_import_product_description_v1"
                    ).put(
                        key = productDescription.key,
                        value = kodeverkToProto(
                            kode = productDescription.key,
                            beskrivelseEn = productDescription.value.first { it.lang == "en" }.name,
                            beskrivelseEs = productDescription.value.first { it.lang == "es" }.name,
                            beskrivelseFr = productDescription.value.first { it.lang == "fr" }.name,
                        ).toByteArray(),
                    )
                }
        }.onFailure {
            logger.warn("putProductDescription feilet med meldingen ${it.message}", it)
        }
    }

    fun putUnitMeasure(unitMeasures: List<UnitMeasure>) {
        runCatching {
            unitMeasures
                .vaskUnitMeasure()
                .map { unitMeasure ->
                    nats.keyValue(
                        "ephyto_import_unit_measure_v1"
                    ).put(
                        key = unitMeasure.code,
                        value = unitMeasureToProto(
                            unitMeasureKode = unitMeasure.code,
                            unitMeasureBeskrivelse = unitMeasure.symbol,
                        ).toByteArray(),
                    )
                }
        }.onFailure {
            logger.warn("putUnitMeasure feilet med meldingen ${it.message}", it)
        }
    }

    private fun kodeverkToProto(kode: String, beskrivelseEn: String, beskrivelseEs: String, beskrivelseFr: String)
            : KodeverkDto =
        KodeverkDto.newBuilder()
            .setBeskrivelseEn(beskrivelseEn)
            .setBeskrivelseEs(beskrivelseEs)
            .setBeskrivelseFr(beskrivelseFr)
            .setReceivedAt(Instant.now().toTimestamp())
            .setKode(kode)
            .build()

    private fun unitMeasureToProto(unitMeasureKode: String, unitMeasureBeskrivelse: String)
            : UnitMeasureDto =
        UnitMeasureDto.newBuilder()
            .setBeskrivelse(unitMeasureBeskrivelse)
            .setReceivedAt(Instant.now().toTimestamp())
            .setKode(unitMeasureKode)
            .build()

    fun putMeanOfTransports(meanOfTransports: List<MeanOfTransport>) {
        runCatching {
            meanOfTransports.forEach { meanOfTransport ->
                nats.keyValue(
                    "ephyto_import_transport_methods_v1"
                ).put(
                    key = "${meanOfTransport.modeCode}/${meanOfTransport.lang}",
                    value = meanOfTransportToProto(meanOfTransport).toByteArray(),
                )
            }
        }.onFailure {
            logger.warn("putMeanOfTransports feilet med meldingen ${it.message}", it)
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
                nats.keyValue(
                    "ephyto_import_treatment_types_v1"
                ).put(
                    key = "${treatmentType.code}/${treatmentType.lang}",
                    value = treatmentTypeToProto(treatmentType).toByteArray(),
                )
            }
        }.onFailure {
            logger.warn("putTreatmentTypes feilet med meldingen ${it.message}", it)
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
