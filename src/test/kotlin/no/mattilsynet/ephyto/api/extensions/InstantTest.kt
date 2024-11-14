package no.mattilsynet.ephyto.api.extensions

import org.junit.jupiter.api.Test
import org.threeten.bp.Instant

class InstantTest {

    @Test
    fun `toTimestamp (protobuf) kjoerer uten problem`() {
        // Given:
        val instant = Instant.ofEpochSecond(1620000000L)

        // When:
        val timestamp = instant.toTimestamp()

        // Then:
        assert(timestamp.seconds == 1620000000L)
    }

}
