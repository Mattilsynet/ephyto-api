package no.mattilsynet.ephyto.api.clients

import _int.ippc.ephyto.DeliveryService
import _int.ippc.ephyto.IDeliveryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EphytoDeliveryService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getClientConnection(): IDeliveryService =
        runCatching {
            DeliveryService().iDeliveryServiceImplPort
        }.getOrElse {
            logger.warn("Kunne ikke hente klient for EphytoDeliveryService, Exception: ${it.message}", it)
            throw it
        }

}
