package no.mattilsynet.ephyto.api.mocks.ephyto

import _int.ippc.ephyto.AllowedDocument
import _int.ippc.ephyto.CertificateStatus
import _int.ippc.ephyto.CertificateType
import _int.ippc.ephyto.ChannelDirection
import _int.ippc.ephyto.ChannelRule
import _int.ippc.ephyto.ChannelRuleType
import _int.ippc.ephyto.SigningCertificate
import _int.ippc.ephyto.hub.ArrayOfChannelRules
import _int.ippc.ephyto.hub.Nppo

object NppoMocker {

    fun createNppoMedAlleData(
        countryCode: String = "DK",
        sertifikat: String = "",
        ): Nppo =
        Nppo().also { nppo ->
            nppo.allowedDocument.add(
                AllowedDocument().also { allowedDocument ->
                    allowedDocument.isActive = true
                    allowedDocument.certificateType = CertificateType().also { type ->
                        type.value = "Test"
                        type.number = 0
                    }
                    allowedDocument.certificateStatus = CertificateStatus().also { status ->
                        status.value = "Test"
                        status.number = 0
                    }
                }
            )
            nppo.channelRules = ArrayOfChannelRules().also { channelRules ->
                channelRules.channelRule.add(
                    ChannelRule().also { channelRule ->
                        channelRule.certificateStatus = 0
                        channelRule.certificateType = 0
                        channelRule.countryCode = countryCode
                        channelRule.direction = ChannelDirection.INC
                        channelRule.id = 0
                        channelRule.isActive = true
                        channelRule.ruleType = ChannelRuleType.FORWARD
                    }
                )
            }
            nppo.country = countryCode
            nppo.receive = "True"
            nppo.send = "True"
            nppo.signature = SigningCertificate().also { signingCertificate ->
                signingCertificate.certificate = sertifikat
                signingCertificate.dn = "CN=Test, OU=Test, O=Test, L=Test, ST=Test, C=DK"
            }
        }

    fun createNppoMockMedLandkode(landkode: String): Nppo =
        Nppo().also { nppo ->
            nppo.country = landkode
        }

    fun createNppoMockMedSignaturSertifikat(sertifikat: String): Nppo =
        Nppo().also { nppo ->
            nppo.signature = SigningCertificate().also {
                it.certificate = sertifikat
            }
        }

    fun createNppoMockUtenSignaturSertifikat(): Nppo =
        Nppo().also { nppo ->
            nppo.signature = null
        }

}
