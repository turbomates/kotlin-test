package com.turbomates.testsupport

import java.time.OffsetDateTime
fun OffsetDateTime.withoutSeconds(): OffsetDateTime {
    return withNano(0).withSecond(0)
}
