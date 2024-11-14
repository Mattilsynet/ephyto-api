package no.mattilsynet.ephyto.api.scheduling

import no.mattilsynet.ephyto.api.services.EphytoKodeverkService
import no.mattilsynet.ephyto.api.services.EphytoService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
internal class EphytoSchedulerTest {

    private lateinit var ephytoScheduler: EphytoScheduler

    @MockBean
    private val ephytoKodeverkService: EphytoKodeverkService = mock()

    @MockBean
    private val ephytoService: EphytoService = mock()


    @BeforeEach
    fun setUp() {
        ephytoScheduler = EphytoScheduler(
            ephytoKodeverkService = ephytoKodeverkService,
            ephytoService = ephytoService,
            sleepTime = 0,
        )
    }

    @Test
    fun `getEnvelopes kaller videre til ephytoService`() {
        // When:
        ephytoScheduler.getEnvelopes()

        // Then:
        verify(ephytoService).hentNyeEnvelopes(0)
    }

    @Test
    fun `getKodeverk kaller videre til ephytoKodeverkService`() {
        // When:
        ephytoScheduler.getKodeverk()

        // Then:
        verify(ephytoKodeverkService, Mockito.times(1)).pushNpposTilNats()
        verify(ephytoKodeverkService, Mockito.times(1)).pushIndendedUseTilNats()
        verify(ephytoKodeverkService, Mockito.times(1)).pushMeanOfTransportsTilNats()
        verify(ephytoKodeverkService, Mockito.times(1)).pushStatementsTilNats()
        verify(ephytoKodeverkService, Mockito.times(1)).pushTreatmentsTilNats()
    }

}
