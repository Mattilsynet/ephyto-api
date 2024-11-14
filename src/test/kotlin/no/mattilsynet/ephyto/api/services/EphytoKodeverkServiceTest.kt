package no.mattilsynet.ephyto.api.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean

internal class EphytoKodeverkServiceTest {

    private lateinit var ephytoKodeverkService: EphytoKodeverkService

    @MockBean
    private val ephytoService = mock<EphytoService>()

    @MockBean
    private val natsKodeverkService = mock<NatsKodeverkService>()

    @BeforeEach
    fun setUp() {
        ephytoKodeverkService = EphytoKodeverkService(ephytoService, natsKodeverkService)
    }

    @Test
        fun `pushTreatmentsTilNats kaller videre til ephytoService og natsService`() {
        // When:
        ephytoKodeverkService.pushTreatmentsTilNats()

        // Then:
        verify(ephytoService, Mockito.times(1)).hentTreatmentTypes()
        verify(natsKodeverkService, Mockito.times(1)).putTreatmentTypes(any())
    }

    @Test
        fun `pushIndendedUseTilNats kaller videre til ephytoService og natsService`() {
        // When:
        ephytoKodeverkService.pushIndendedUseTilNats()

        // Then:
        verify(ephytoService, Mockito.times(1)).hentIndendedUse()
        verify(natsKodeverkService, Mockito.times(1)).putIntendedUse(any())
    }

    @Test
    fun `pushNpposTilNats kaller videre til ephytoService og natsService`() {
        // When:
        ephytoKodeverkService.pushNpposTilNats()

        // Then:
        verify(ephytoService, Mockito.times(1)).hentAktiveNppos()
        verify(natsKodeverkService, Mockito.times(1)).putNppos(any())
    }

    @Test
    fun `pushStatementsTilNats kaller videre til ephytoService og natsService`() {
        // When:
        ephytoKodeverkService.pushStatementsTilNats()

        // Then:
        verify(ephytoService, Mockito.times(1)).hentStatements()
        verify(natsKodeverkService, Mockito.times(1)).putStatements(any())
    }

    @Test
    fun `pushMeanOfTransportsTilNats kaller videre til ephytoService og natsService`() {
        // When:
        ephytoKodeverkService.pushMeanOfTransportsTilNats()

        // Then:
        verify(ephytoService, Mockito.times(1)).hentMeanOfTransports()
        verify(natsKodeverkService, Mockito.times(1)).putMeanOfTransports(any())
    }

}
