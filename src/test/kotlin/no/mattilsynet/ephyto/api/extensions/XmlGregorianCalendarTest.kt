package no.mattilsynet.ephyto.api.extensions

import java.util.*
import javax.xml.datatype.DatatypeFactory
import org.junit.jupiter.api.Test

class XmlGregorianCalendarTest {

    @Test
    fun `toTimestamp (protobuf) kjoerer uten problem`() {
        // Given:
        val calendar = GregorianCalendar(2021, 8, 1, 0, 0, 0)
        val xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar)

        // When:
        val timestamp = xmlGregorianCalendar.toTimestamp()

        // Then:
        assert(timestamp.seconds > 0)
    }


}

