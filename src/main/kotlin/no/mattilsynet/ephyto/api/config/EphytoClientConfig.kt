package no.mattilsynet.ephyto.api.config

import no.mattilsynet.ephyto.api.clients.EphytoClientAcknowledge
import no.mattilsynet.ephyto.api.clients.EphytoClientLogAcknowledge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class EphytoClientConfig {

    @Bean
    @Profile("prod", "dev", "local")
    fun ephytoClientAcknowledge() = EphytoClientAcknowledge()

    @Bean
    @Primary
    @Profile("test", "staging")
    fun ephytoClientLogAcknowledge() = EphytoClientLogAcknowledge()

}
