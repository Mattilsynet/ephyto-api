package no.mattilsynet.ephyto.api.controllers.mock

import _int.ippc.ephyto.ActualDateTime
import _int.ippc.ephyto.AffixedSPSSeal
import _int.ippc.ephyto.ApplicableSPSClassification
import _int.ippc.ephyto.ApplicableSPSProcessCharacteristic
import _int.ippc.ephyto.BinaryObjectType
import _int.ippc.ephyto.CompletionSPSPeriod
import _int.ippc.ephyto.ConsigneeSPSParty
import _int.ippc.ephyto.ConsignorSPSParty
import _int.ippc.ephyto.EndDateTime
import _int.ippc.ephyto.IssueSPSLocation
import _int.ippc.ephyto.MeasureType
import _int.ippc.ephyto.ObjectFactory
import _int.ippc.ephyto.ProviderSPSParty
import _int.ippc.ephyto.ReferenceSPSReferencedDocument
import _int.ippc.ephyto.SPSCertificate
import _int.ippc.ephyto.SignatorySPSAuthentication
import _int.ippc.ephyto.SpecifiedSPSPerson
import _int.ippc.ephyto.SpsIssuerSPSParty
import _int.ippc.ephyto.StartDateTime
import _int.ippc.ephyto.SubordinateSPSCountrySubDivision
import _int.ippc.ephyto.TransitSPSCountry
import _int.ippc.ephyto.UnloadingBaseportSPSLocation
import _int.ippc.ephyto.UsedSPSTransportMeans
import _int.ippc.ephyto.UtilizedSPSTransportEquipment
import _int.ippc.ephyto.hub.ArrayOfEnvelopeForwarding
import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.EnvelopeHeader
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.Marshaller
import no.mattilsynet.ephyto.api.clients.EphytoDeliveryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._21.IncludedSPSNoteContent
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._21.IncludedSPSTradeLineItem
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._21.SpsConsignment
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._21.SpsExchangedDocument
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._21.TextType
import java.io.StringWriter
import java.time.LocalDateTime
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt
import _int.ippc.ephyto.hub.ObjectFactory as EphytoObjectFactory
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._21.ObjectFactory as ReusableObjectFactory
import un.unece.uncefact.data.standard.spscertificate._17.ObjectFactory as SpsCertificateObjectFactory

@Service
@Suppress("TooManyFunctions")
class CreateEphytoMockdataService(
    private val ephytoDeliveryService: EphytoDeliveryService,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    var reusableObjectFactory = ReusableObjectFactory()

    fun sendEphytoEnvelope(
        antall: Int,
        erstatterSertifikatNummer: String? = null,
        status: Int? = null,
        type: Int? = null,
    ) {
        repeat(antall) {
            deliverEnvelope(
                createEnvelope(
                    erstatterSertifikatNummer = erstatterSertifikatNummer,
                    status = status,
                    type = type,
                )
            )
        }
    }

    private fun deliverEnvelope(envelope: Envelope): EnvelopeHeader? =
        runCatching {
            ephytoDeliveryService.getClientConnection().deliverEnvelope(envelope)

        }.onFailure {
            logger.warn(
                "Kunne ikke levere sertifikat. Exception: ${it.message}",
                it
            )
        }.getOrNull()

    @Suppress("MagicNumber")
    fun createEnvelope(erstatterSertifikatNummer: String?, status: Int?, type: Int?): Envelope {
        val hubEphytoObjectFactory = EphytoObjectFactory()

        val sertifikatType = createSertifikatType(type)

        val from = Pair("NO", "Norge")
        val to = Pair("NO", "Norge")
        val nppoCertificateNumber = "TEST-${from.first}.${to.first}.${nextInt(99999)}"
        val envelope = hubEphytoObjectFactory.createEnvelope().also { ePhytoEnvelope ->
            ePhytoEnvelope.certificateStatus = status ?: 70
            ePhytoEnvelope.certificateType = sertifikatType
            ePhytoEnvelope.from = from.first
            ePhytoEnvelope.hubDeliveryErrorMessage = ""
            ePhytoEnvelope.hubTrackingInfo = ""
            ePhytoEnvelope.hubDeliveryNumber = ""
            ePhytoEnvelope.nppoCertificateNumber = nppoCertificateNumber
            ePhytoEnvelope.to = to.first
            ePhytoEnvelope.forwardings = ArrayOfEnvelopeForwarding()
        }

        val spsCertificate = createSPSCertificate(
            erstatterSertifikatNummer = erstatterSertifikatNummer,
            from = from,
            nppoCertificateNumber = nppoCertificateNumber,
            sertifikatType = sertifikatType,
            to = to,
        )

        val stringWriter = StringWriter()
        val marshaller = JAXBContext.newInstance(SPSCertificate::class.java).createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(spsCertificate, stringWriter)

        envelope.content = stringWriter.toString()
        return envelope
    }

    @Suppress("MagicNumber")
    private fun createSertifikatType(type: Int?) = type ?: when (nextInt(15) > 1) {
        true -> 851
        else -> 657
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod", "MagicNumber")
    private fun createSPSCertificate(
        erstatterSertifikatNummer: String?,
        from: Pair<String, String>,
        nppoCertificateNumber: String,
        sertifikatType: Int,
        to: Pair<String, String>,
    ): JAXBElement<SPSCertificate>? {
        val ephytoObjectFactory = ObjectFactory()

        val spsCertificateObjectFactory = SpsCertificateObjectFactory()
        val offsetDateTime = LocalDateTime.now().toString()

        return spsCertificateObjectFactory.createSPSCertificate(
            SPSCertificate().also { spsCertificate ->
                spsCertificate.spsConsignment = SpsConsignment().also { spsConsignment ->
                    spsConsignment.consigneeSPSParty =
                        ephytoObjectFactory.createConsigneeSPSParty().also { consigneeSPSParty ->
                            createMottaker(
                                consigneeSPSParty = consigneeSPSParty,
                                ephytoObjectFactory = ephytoObjectFactory,
                            )
                        }
                    spsConsignment.consignorSPSParty =
                        ephytoObjectFactory.createConsignorSPSParty().also { consignorSPSParty ->
                            createAvsender(
                                consignorSPSParty = consignorSPSParty,
                                ephytoObjectFactory = ephytoObjectFactory,
                            )
                        }
                    spsConsignment.examinationSPSEvent =
                        reusableObjectFactory.createExaminationSPSEvent().also { examinationSPSEvent ->
                            examinationSPSEvent.occurrenceSPSLocation =
                                ephytoObjectFactory.createOccurrenceSPSLocation().also { occurrenceSPSLocation ->
                                    occurrenceSPSLocation.name = "occurrenceSPSLocation"
                                }
                        }
                    spsConsignment.exportSPSCountry = reusableObjectFactory.createExportSPSCountry().also {
                        it.id = from.first
                        it.name = from.second
                    }
                    spsConsignment.importSPSCountry = reusableObjectFactory.createImportSPSCountry().also {
                        it.id = to.first
                        it.name = to.second
                    }

                    repeat((1..nextInt(1, 15)).count()) {
                        spsConsignment.includedSPSConsignmentItem.add(
                            getVarelinje(
                                offsetDateTime = offsetDateTime,
                                plantenavn = getRandomScientificName(),
                                sertifikatType = sertifikatType,
                                to = to,
                            )
                        )
                    }

                    spsConsignment.mainCarriageSPSTransportMovement.add(
                        reusableObjectFactory.createMainCarriageSPSTransportMovement()
                            .also { mainCarriageSPSTransportMovement ->
                                mainCarriageSPSTransportMovement.id = "Voyage N° 18"
                                mainCarriageSPSTransportMovement.modeCode = 1
                                mainCarriageSPSTransportMovement.usedSPSTransportMeans =
                                    UsedSPSTransportMeans().also { usedSPSTransportMeans ->
                                        usedSPSTransportMeans.name = "Ocean Vessel: Black Pearl"
                                    }
                            }
                    )

                    takeIf { nextInt(8) > 1 }?.let {
                        spsConsignment.transitSPSCountry.add(
                            TransitSPSCountry().also { transitSPSCountry ->
                                getRandomLand().let { land ->
                                    transitSPSCountry.id = land.first
                                    transitSPSCountry.name = land.second
                                }
                            }
                        )
                    }

                    spsConsignment.unloadingBaseportSPSLocation =
                        UnloadingBaseportSPSLocation().also { unloadingBaseportSPSLocation ->
                            unloadingBaseportSPSLocation.id = "USSEA"
                            unloadingBaseportSPSLocation.name = "Guayaquil (Seattle)"
                        }
                    spsConsignment.utilizedSPSTransportEquipment =
                        UtilizedSPSTransportEquipment().also { utilizedSPSTransportEquipment ->
                            utilizedSPSTransportEquipment.affixedSPSSeal = AffixedSPSSeal().also { affixedSPSSeal ->
                                affixedSPSSeal.id = "G2382564"
                            }
                            utilizedSPSTransportEquipment.id = null
                        }
                }

                spsCertificate.spsExchangedDocument = SpsExchangedDocument().also { spsExchangedDocument ->
                    spsExchangedDocument.id = nppoCertificateNumber
                    spsExchangedDocument.includedSPSNote.add(
                        ephytoObjectFactory.createIncludedSPSNote().also { includedSPSNote ->
                            includedSPSNote.content = IncludedSPSNoteContent().also { includedSPSNoteContent ->
                                includedSPSNoteContent.languageID = null
                                includedSPSNoteContent.value = "5"
                            }
                            includedSPSNote.subject = "SPSFL"
                        }
                    )
                    spsExchangedDocument.includedSPSNote.add(
                        ephytoObjectFactory.createIncludedSPSNote().also { includedSPSNote ->
                            includedSPSNote.content = IncludedSPSNoteContent().also { includedSPSNoteContent ->
                                includedSPSNoteContent.languageID = null
                                includedSPSNoteContent.value = "Phyllocnistiscitrella is absent in Argentina"
                            }
                            includedSPSNote.subject = "ADEDL"
                        }
                    )
                    spsExchangedDocument.includedSPSNote.add(
                        ephytoObjectFactory.createIncludedSPSNote().also { includedSPSNote ->
                            includedSPSNote.content = IncludedSPSNoteContent().also { includedSPSNoteContent ->
                                includedSPSNoteContent.languageID = null
                                includedSPSNoteContent.value = "kjennetegn"
                            }
                            includedSPSNote.subject = "DMCL"
                        }
                    )
                    erstattSertifikat(
                        ephytoObjectFactory = ephytoObjectFactory,
                        erstatterSertifikatNummer = erstatterSertifikatNummer,
                        spsExchangedDocument = spsExchangedDocument,
                    )
                    spsExchangedDocument.issueDateTime = offsetDateTime
                    spsExchangedDocument.issuerSPSParty = SpsIssuerSPSParty().also {
                        it.name = "Mattilsynet"
                    }
                    spsExchangedDocument.name = null

                    when {
                        sertifikatType == 657 ->
                            createVedlegg(spsExchangedDocument, nppoCertificateNumber, offsetDateTime)

                    }
                    spsExchangedDocument.signatorySPSAuthentication =
                        SignatorySPSAuthentication().also { signatorySPSAuthentication ->
                            signatorySPSAuthentication.actualDateTime = reusableObjectFactory.createActualDateTime(
                                ActualDateTime().also { it.dateTimeString = offsetDateTime }
                            ).value
                            signatorySPSAuthentication.includedSPSClause.add(
                                reusableObjectFactory.createIncludedSPSClause().also {
                                    it.content = "They are deemed to be practically free from other pests."
                                    it.id = 2
                                }
                            )
                            signatorySPSAuthentication.issueSPSLocation = IssueSPSLocation().also { issueSPSLocation ->
                                issueSPSLocation.name = "Oslo"
                            }
                            signatorySPSAuthentication.providerSPSParty = ProviderSPSParty().also { providerSPSParty ->
                                providerSPSParty.name = null
                                providerSPSParty.specifiedSPSPerson = SpecifiedSPSPerson().also { specifiedSPSPerson ->
                                    specifiedSPSPerson.name = "Ansatt i Mattilsynet"
                                }
                            }
                        }
                    spsExchangedDocument.statusCode = 70
                    spsExchangedDocument.typeCode = sertifikatType
                }
            }
        )
    }

    private fun createVedlegg(
        spsExchangedDocument: SpsExchangedDocument,
        nppoCertificateNumber: String,
        offsetDateTime: String
    ) {
        spsExchangedDocument.referenceSPSReferencedDocument.add(
            ReferenceSPSReferencedDocument().also { referenceSPSReferencedDocument ->
                referenceSPSReferencedDocument.attachmentBinaryObject =
                    BinaryObjectType().also { binaryObjectType ->
                        binaryObjectType.filename = "letter.pdf"
                        binaryObjectType.value = "fileContent"
                    }
                referenceSPSReferencedDocument.id = nppoCertificateNumber
                referenceSPSReferencedDocument.information = TextType().also {
                    it.languageID = "EN"
                    it.value = "Letter of Authority"
                }
                referenceSPSReferencedDocument.issueDateTime = offsetDateTime
                referenceSPSReferencedDocument.relationshipTypeCode = "ZZZ"
                referenceSPSReferencedDocument.typeCode = "ZZZ"
            }
        )
    }

    private fun createAvsender(
        consignorSPSParty: ConsignorSPSParty,
        ephytoObjectFactory: ObjectFactory,
    ) {
        consignorSPSParty.name = avsendere[nextInt(avsendere.size)]
        consignorSPSParty.specifiedSPSAddress =
            createSPSAddressAvsender(
                ephytoObjectFactory = ephytoObjectFactory,
            )
    }

    private fun createMottaker(
        consigneeSPSParty: ConsigneeSPSParty,
        ephytoObjectFactory: ObjectFactory,
    ) {
        val mottaker = mottakere[nextInt(mottakere.size)]
        val mottakernavn = mottaker.second
        consigneeSPSParty.name = mottakernavn
        consigneeSPSParty.specifiedSPSAddress =
            createSPSAddressMottaker(
                ephytoObjectFactory = ephytoObjectFactory,
            )
        consigneeSPSParty.typeCode.addAll(listOf(mottaker.first))
    }

    private fun erstattSertifikat(
        ephytoObjectFactory: ObjectFactory,
        erstatterSertifikatNummer: String?,
        spsExchangedDocument: SpsExchangedDocument,
    ) {
        if (erstatterSertifikatNummer != null) {
            spsExchangedDocument.includedSPSNote.add(
                ephytoObjectFactory.createIncludedSPSNote().also { includedSPSNote ->
                    includedSPSNote.content = IncludedSPSNoteContent().also { includedSPSNoteContent ->
                        includedSPSNoteContent.languageID = null
                        includedSPSNoteContent.value = erstatterSertifikatNummer
                    }
                    includedSPSNote.subject = "ADRPN"
                }
            )
        }
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod", "MagicNumber", "TooManyParameters")
    private fun getVarelinje(
        offsetDateTime: String,
        plantenavn: String,
        sertifikatType: Int,
        to: Pair<String, String>,
    ) =
        reusableObjectFactory.createIncludedSPSConsignmentItem().also { includedSPSConsignmentItem ->
            includedSPSConsignmentItem.includedSPSTradeLineItem.add(
                reusableObjectFactory.createIncludedSPSTradeLineItem()
                    .also { includedSPSTradeLineItem ->

                        includedSPSTradeLineItem.additionalInformationSPSNote.add(
                            reusableObjectFactory.createAdditionalInformationSPSNote()
                                .also { additionalInformationSPSNote ->
                                    additionalInformationSPSNote.content.add(
                                        reusableObjectFactory.createTextType().also { textType ->
                                            textType.languageID = "EN"
                                            textType.value = "Content"
                                        }
                                    )
                                    additionalInformationSPSNote.subject = "Subject"
                                }
                        )
                        addKjennetegn(includedSPSTradeLineItem)
                        includedSPSTradeLineItem.applicableSPSClassification.add(
                            ApplicableSPSClassification().also { applicableSPSClassification ->
                                applicableSPSClassification.classCode = "123"
                                applicableSPSClassification.className.add(
                                    reusableObjectFactory.createTextType().also { textType ->
                                        textType.languageID = "EN"
                                        textType.value = "ClassName"
                                    }
                                )
                                applicableSPSClassification.systemName = "SystemName"
                            }
                        )
                        includedSPSTradeLineItem.applicableSPSClassification.add(
                            ApplicableSPSClassification().also { applicableSPSClassification ->
                                applicableSPSClassification.className.add(
                                    reusableObjectFactory.createTextType().also { textType ->
                                        textType.languageID = "EN"
                                        textType.value = getRandomPlantedel()
                                    }
                                )
                                applicableSPSClassification.systemName = "IPPCPCVP"
                            }
                        )
                        includedSPSTradeLineItem.applicableSPSClassification.add(
                            ApplicableSPSClassification().also { applicableSPSClassification ->
                                applicableSPSClassification.className.add(
                                    reusableObjectFactory.createTextType().also { textType ->
                                        textType.languageID = "EN"
                                        textType.value = getRandomTilstand()
                                    }
                                )
                                applicableSPSClassification.systemName = "IPPCPCC"
                            }
                        )

                        includedSPSTradeLineItem.appliedSPSProcess.addAll(
                            listOf(
                                reusableObjectFactory.createAppliedSPSProcess()
                                    .also { appliedSPSProcess ->
                                        appliedSPSProcess.applicableSPSProcessCharacteristic.add(
                                            ApplicableSPSProcessCharacteristic()
                                                .also { applicableSPSProcessCharacteristic ->
                                                    applicableSPSProcessCharacteristic.description
                                                        .addAll(
                                                            listOf(
                                                                reusableObjectFactory.createTextType()
                                                                    .also { textType ->
                                                                        textType.languageID = "en"
                                                                        textType.value = "TTL1"
                                                                    },
                                                                reusableObjectFactory.createTextType()
                                                                    .also { textType ->
                                                                        textType.languageID = "en"
                                                                        textType.value = "CHT"
                                                                    }
                                                            )
                                                        )
                                                    applicableSPSProcessCharacteristic.valueMeasure =
                                                        getMeasureType(unitKode = "KGM")
                                                }
                                        )
                                        appliedSPSProcess.completionSPSPeriod =
                                            getCompletionSPSPeriod(offsetDateTime)
                                        appliedSPSProcess.typeCode = "ZZZ"
                                    },
                                reusableObjectFactory.createAppliedSPSProcess()
                                    .also { appliedSPSProcess ->
                                        appliedSPSProcess.applicableSPSProcessCharacteristic.add(
                                            ApplicableSPSProcessCharacteristic()
                                                .also { applicableSPSProcessCharacteristic ->
                                                    applicableSPSProcessCharacteristic.description
                                                        .addAll(
                                                            listOf(
                                                                reusableObjectFactory.createTextType()
                                                                    .also { textType ->
                                                                        textType.languageID = "en"
                                                                        textType.value = "TTL2"
                                                                    },
                                                                reusableObjectFactory.createTextType()
                                                                    .also { textType ->
                                                                        textType.languageID = "en"
                                                                        textType.value = "FG"
                                                                    }
                                                            )
                                                        )
                                                    applicableSPSProcessCharacteristic.valueMeasure =
                                                        getMeasureType(unitKode = "KGM")
                                                }
                                        )
                                        appliedSPSProcess.completionSPSPeriod =
                                            getCompletionSPSPeriod(offsetDateTime)
                                        appliedSPSProcess.typeCode = "ZZZ"
                                    }
                            ),

                            )

                        includedSPSTradeLineItem.commonName.add(
                            reusableObjectFactory.createTextType().also { textType ->
                                textType.languageID = to.first
                                textType.value = "$plantenavn - navn"
                            }
                        )
                        includedSPSTradeLineItem.description.add(
                            reusableObjectFactory.createTextType().also { textType ->
                                textType.languageID = "EN"
                                textType.value = "Varelinjebeskrivelse. Plantenavnet er $plantenavn"
                            }
                        )
                        includedSPSTradeLineItem.grossVolumeMeasure = getMeasureType(unitKode = "MTR")
                        includedSPSTradeLineItem.grossWeightMeasure =
                            getMeasureType(unitKode = "KGM")

                        if (sertifikatType == 657) createReekport(includedSPSTradeLineItem)

                        repeat((1..nextInt(1, 3)).count()) {
                            includedSPSTradeLineItem.intendedUse.add(
                                reusableObjectFactory.createTextType().also {
                                    it.languageID = "EN"
                                    it.value = getRandomIndendedUse()
                                }
                            )
                        }

                        includedSPSTradeLineItem.netVolumeMeasure = getMeasureType(unitKode = "MTR")
                        includedSPSTradeLineItem.netWeightMeasure = getMeasureType(unitKode = "KGM")
                        createOpprinnelsesland(includedSPSTradeLineItem)
                        createTilleggserklaering(includedSPSTradeLineItem)
                        includedSPSTradeLineItem.physicalSPSPackage.add(
                            reusableObjectFactory.createPhysicalSPSPackage()
                                .also { physicalSPSPackage ->
                                    physicalSPSPackage.itemQuantity = 1.0
                                    physicalSPSPackage.levelCode = 1
                                    physicalSPSPackage.typeCode = "BG"
                                }
                        )
                        includedSPSTradeLineItem.scientificName.add(
                            reusableObjectFactory.createTextType().also { textType ->
                                textType.languageID = null
                                textType.value = plantenavn
                            }
                        )
                        includedSPSTradeLineItem.sequenceNumeric = 0
                    }
            )
            includedSPSConsignmentItem.natureIdentificationSPSCargo =
                reusableObjectFactory.createSpsCargoType().also { spsCargoType ->
                    spsCargoType.typeCode = "123"
                }
        }

    @Suppress("MagicNumber")
    private fun addKjennetegn(includedSPSTradeLineItem: IncludedSPSTradeLineItem) {
        repeat((1..nextInt(0, 5)).count()) {
            includedSPSTradeLineItem.additionalInformationSPSNote.add(
                reusableObjectFactory.createAdditionalInformationSPSNote()
                    .also { additionalInformationSPSNote ->
                        additionalInformationSPSNote.content.add(
                            reusableObjectFactory.createTextType().also { textType ->
                                textType.languageID = "EN"
                                textType.value = "Kjennetegn1-$it ${nextInt()}"
                            }
                        )
                        additionalInformationSPSNote.content.add(
                            reusableObjectFactory.createTextType().also { textType ->
                                textType.languageID = "EN"
                                textType.value = "Kjennetegn2-$it ${nextInt()}"
                            }
                        )
                        additionalInformationSPSNote.subject = "DMTLIL"
                    }
            )
        }
    }

    @Suppress("MagicNumber")
    private fun createOpprinnelsesland(includedSPSTradeLineItem: IncludedSPSTradeLineItem) {
        repeat((1..nextInt(1, 3)).count()) {
            includedSPSTradeLineItem.originSPSCountry.add(
                reusableObjectFactory.createOriginSPSCountry().also { originSPSCountry ->
                    val opprinnelsesland = getRandomLand()
                    opprinnelsesland.let {
                        originSPSCountry.id = it.first
                        originSPSCountry.name = it.second
                    }

                    when {
                        nextBoolean() -> {
                            originSPSCountry.subordinateSPSCountrySubDivision =
                                SubordinateSPSCountrySubDivision()
                                    .also { subordinateSPSCountrySubDivision ->
                                        subordinateSPSCountrySubDivision.hierarchicalLevelCode = "0"
                                        subordinateSPSCountrySubDivision.name =
                                            "Landsdel for ${opprinnelsesland.second}"
                                    }
                        }
                    }
                }
            )
        }
    }

    @Suppress("MagicNumber")
    private fun createReekport(includedSPSTradeLineItem: IncludedSPSTradeLineItem) {
        includedSPSTradeLineItem.additionalInformationSPSNote.add(
            createAdditionalSPSNote(subject = "RPCST", value = "657")
        )
        includedSPSTradeLineItem.additionalInformationSPSNote.add(
            createAdditionalSPSNote(subject = "RPCCO", value = getRandomLand().first)
        )
        includedSPSTradeLineItem.additionalInformationSPSNote.add(
            createAdditionalSPSNote(subject = "RPCRF", value = nextInt(999999).toString())
        )
        includedSPSTradeLineItem.additionalInformationSPSNote.add(
            createAdditionalSPSNote(subject = "RPCOR", value = "FALSE")
        )
        includedSPSTradeLineItem.additionalInformationSPSNote.add(
            createAdditionalSPSNote(subject = "RPCTC", value = "FALSE")
        )
    }

    @Suppress("MagicNumber")
    private fun createTilleggserklaering(includedSPSTradeLineItem: IncludedSPSTradeLineItem) {
        repeat((1..nextInt(1, 3)).count()) {
            getTilleggserklaeringVarelinje().let { tilleggserklaeringVarelinje ->
                includedSPSTradeLineItem.additionalInformationSPSNote.add(
                    createAdditionalSPSNote(
                        subject = tilleggserklaeringVarelinje,
                        value = "Tilleggserklæring $tilleggserklaeringVarelinje",
                    )
                )
            }
        }
    }

    private fun createAdditionalSPSNote(subject: String, value: String) =
        reusableObjectFactory.createAdditionalInformationSPSNote()
            .also { additionalInformationSPSNote ->
                additionalInformationSPSNote.subject = subject
                additionalInformationSPSNote.content.add(
                    reusableObjectFactory.createTextType().also { textType ->
                        textType.languageID = "EN"
                        textType.value = value
                    }
                )
            }

    private fun getMeasureType(unitKode: String) =
        MeasureType().also { measureType ->
            measureType.unitCode = unitKode
            measureType.value = getRandomValueAsDouble()
        }

    private fun getCompletionSPSPeriod(offsetDateTime: String) =
        CompletionSPSPeriod().also { completionSPSPeriod ->
            completionSPSPeriod.durationMeasure =
                MeasureType().also { measureType ->
                    measureType.unitCode = "MIN"
                    measureType.value = getRandomValueAsDouble()
                }
            completionSPSPeriod.endDateTime =
                EndDateTime().also { endDateTime ->
                    endDateTime.dateTimeString = offsetDateTime
                }
            completionSPSPeriod.startDateTime =
                StartDateTime().also { startDateTime ->
                    startDateTime.dateTimeString = offsetDateTime
                }
        }

    private fun createSPSAddressMottaker(
        ephytoObjectFactory: ObjectFactory,
    ) =
        ephytoObjectFactory.createSpecifiedSPSAddress().also { specifiedSPSAddress ->
            specifiedSPSAddress.lineOne = "Roseveien 15"
            specifiedSPSAddress.lineTwo = "5555 Rosendal"
            specifiedSPSAddress.lineThree = "Norge"
        }

    private fun createSPSAddressAvsender(
        ephytoObjectFactory: ObjectFactory,
    ) =
        ephytoObjectFactory.createSpecifiedSPSAddress().also { specifiedSPSAddress ->
            specifiedSPSAddress.lineOne = "Plantegata 1"
            specifiedSPSAddress.lineTwo = "15015 Paris"
            specifiedSPSAddress.lineTwo = getRandomLand().second
        }

    private fun getRandomScientificName() = botaniskNavn[nextInt(botaniskNavn.size)]
    private fun getRandomLand() = land[nextInt(land.size)]

    private fun getTilleggserklaeringVarelinje() =
        tilleggserklaeringerVarelinje[nextInt(tilleggserklaeringerVarelinje.size)]

    private fun getRandomIndendedUse() = indendedUse[nextInt(indendedUse.size)]

    private fun getRandomPlantedel() = plantedel[nextInt(plantedel.size)]

    private fun getRandomTilstand() = tilstand[nextInt(tilstand.size)]

    @Suppress("MagicNumber")
    private fun getRandomValueAsDouble() = nextInt(99).toDouble()

    companion object {
        val land = listOf(
            Pair("DE", "Tyskland"),
            Pair("DK", "Danmark"),
            Pair("ES", "Spania"),
            Pair("FR", "Frankrike"),
            Pair("NL", "Nederland"),
            Pair("PK", "Pakistan"),
            Pair("PT", "Portugal"),
            Pair("SE", "Sverige"),
            Pair("UG", "Uganda"),
            Pair("US", "USA"),
            Pair("ZA", "Sør-Afrika"),
        )

        val botaniskNavn = listOf(
            "Acer macrophyllum",
            "Araujia odorata",
            "Cichorium intybus",
            "Fragaria x ananassa",
            "Malus domestica",
            "Solanum lycopersicum",
        )

        val indendedUse = listOf(
            "Consumption",
            "Decoration",
            "Planting",
            "Plants",
        )

        val plantedel = listOf(
            "bulk",
            "FRUIT",
            "Seeds",
            "plantedel",
            "seeds",
            "wood packaging material",
        )

        val tilstand = listOf(
            "fresh",
            "Potted",
        )

        val mottakere = listOf(
            Pair("313200884", "MOTIVERT USNOBBET TIGER AS"),
            Pair("215297802", "MOTIVERT USNOBBET TIGER AS"),
            Pair("310968463", "SKÅNSOM DIREKTE KROKODLE"),
            Pair("310968463", "SKÅNSOM DIREKTE KROKODILLE"),
            Pair("314737547", "SKÅNSOM DIREKTE KROKODILLE"),
            Pair("313155528", "UFØLSOM NESTE TIGER"),
            Pair("313155528", "UFØLSOM NESTE TIGER AS"),
            Pair("315682789", "UFØLSOM NESTE TIGER"),
        )

        val avsendere = listOf(
            "Fantastisk blomsterprodusent",
            "Planteeksperten eksport",
            "Eksportgruppen planteland",
        )

        val tilleggserklaeringerVarelinje = listOf(
            "ADAOTLIL",
            "ADIPTLIL",
            "ADTLIL",
        )
    }

}
