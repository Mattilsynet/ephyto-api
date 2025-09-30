package no.mattilsynet.ephyto.api

import no.mattilsynet.fisk.libs.spring.virtualnats.SpringVirtualNatsStarter
import no.mattilsynet.fisk.libs.spring.virtualnats.wrapper.LoggerVirtualNatsWrapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties
@EnableScheduling
@Import(
    LoggerVirtualNatsWrapper::class,
    SpringVirtualNatsStarter::class,
)
class EphytoApiApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    EphytoKeystorePropertySetter().setSystemJavaKeystore()
    runApplication<EphytoApiApplication>(*args)
}
