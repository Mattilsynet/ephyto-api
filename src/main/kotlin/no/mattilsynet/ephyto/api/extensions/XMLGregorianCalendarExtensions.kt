package no.mattilsynet.ephyto.api.extensions

import com.google.protobuf.Timestamp
import javax.xml.datatype.XMLGregorianCalendar

@Suppress("MagicNumber")
fun XMLGregorianCalendar.toTimestamp(): Timestamp =
    this.toGregorianCalendar().toInstant().let { instant ->
        Timestamp.newBuilder()
            .setSeconds(instant.epochSecond)
            .setNanos(instant.nano)
            .build()

    }

