package no.mattilsynet.ephyto.api.extensions

import com.google.protobuf.Timestamp
import org.threeten.bp.Instant


@Suppress("MagicNumber")
fun Instant.toTimestamp(): Timestamp =
    Timestamp.newBuilder()
        .setSeconds(epochSecond)
        .setNanos(nano)
        .build()

