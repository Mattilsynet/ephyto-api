package no.mattilsynet.ephyto.api.domain

import _int.ippc.ephyto.hub.Envelope
import _int.ippc.ephyto.hub.HUBTrackingInfo

data class Valideringsresultat(
    val envelope: Envelope?,
    val errorMessage: String?,
    val hubLeveringNummer: String,
    val hubTrackingInfo: HUBTrackingInfo,
    val validatedOk: Boolean,
)
