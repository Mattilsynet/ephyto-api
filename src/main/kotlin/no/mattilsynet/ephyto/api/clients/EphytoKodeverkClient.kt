package no.mattilsynet.ephyto.api.clients

import _int.ippc.ephyto.IntendedUse
import _int.ippc.ephyto.MeanOfTransport
import _int.ippc.ephyto.Statement
import _int.ippc.ephyto.TreatmentType
import _int.ippc.ephyto.hub.ArrayOfNppo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EphytoKodeverkClient {

    @Autowired
    private lateinit var ephytoDeliveryService: EphytoDeliveryService

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getMeanOfTransports(): List<MeanOfTransport> =
        runCatching {
            ephytoDeliveryService.getClientConnection().meanOfTransports
        }.getOrElse {
            logger.warn("Kunne ikke hente transportmetoder fra ephyto. Exception: ${it.message}",
                it)
            emptyList()
        }

    fun getIndendedUse(): List<IntendedUse> =
        runCatching {
            ephytoDeliveryService.getClientConnection().intendedUses
        }.getOrElse {
            logger.warn("Kunne ikke hente tilsiktet bruk fra ephyto. Exception: ${it.message}",
                it)
            emptyList()
        }

    fun getStatements(): List<Statement> =
        runCatching {
            ephytoDeliveryService.getClientConnection().statements
        }.getOrElse {
            logger.warn(it.message, it)
            emptyList()
        }

    fun getTreatmentTypes(): List<TreatmentType> =
        runCatching {
            ephytoDeliveryService.getClientConnection().treatmentTypes
        }.getOrElse {
            logger.warn(it.message, it)
            emptyList()
        }

    fun getActiveNppos(): ArrayOfNppo? = runCatching {
        ephytoDeliveryService.getClientConnection().activeNppos
    }.onFailure {
        logger.warn(it.message, it)
    }.getOrNull()

}
