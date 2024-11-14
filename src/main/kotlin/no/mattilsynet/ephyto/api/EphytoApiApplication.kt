package no.mattilsynet.ephyto.api

import no.mattilsynet.fisk.libs.springnats.spring.SpringNatsConfiguration
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
@Import(SpringNatsConfiguration::class)
class EphytoApiApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
	EphytoKeystorePropertySetter().setSystemJavaKeystore()
	runApplication<EphytoApiApplication>(*args)
}
