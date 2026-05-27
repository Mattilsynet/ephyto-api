package no.mattilsynet.ephyto.api.nats.logger

import no.mattilsynet.virtualnats.virtualnatsspring.wrapper.LoggerVirtualNatsWrapper
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class NatsLogger: ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        LoggerVirtualNatsWrapper()
    }
}
