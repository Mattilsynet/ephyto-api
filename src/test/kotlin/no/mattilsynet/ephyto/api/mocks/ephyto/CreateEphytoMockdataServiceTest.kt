package no.mattilsynet.ephyto.api.mocks.ephyto

import no.mattilsynet.ephyto.api.clients.EphytoDeliveryService
import no.mattilsynet.ephyto.api.controllers.mock.CreateEphytoMockdataService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.test.mock.mockito.MockBean

internal class CreateEphytoMockdataServiceTest {

    @MockBean
    val ephytoDeliveryService = mock<EphytoDeliveryService>()

    private lateinit var createEphytoMockdataService: CreateEphytoMockdataService

    @BeforeEach
    fun setUp() {
        createEphytoMockdataService = CreateEphytoMockdataService(ephytoDeliveryService)
    }

    @Test
    fun `sendEphytoEnvelope kjoerer createEnvelope suksessfullt`() {
        // When & Then:
        createEphytoMockdataService.sendEphytoEnvelope(antall = 1)
    }
}
