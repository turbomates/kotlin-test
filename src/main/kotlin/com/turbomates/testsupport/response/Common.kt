package com.turbomates.testsupport.response

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

suspend fun HttpResponse.assert(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    block: suspend HttpResponse.() -> Unit = {}
) {
    withClue(bodyAsText()) {
        assertSoftly {
            status shouldBe statusCode
            block()
        }
    }
}

suspend fun HttpResponse.assertIsOk() = assert()

fun HttpResponse.containsHeader(header: String, value: Any) {
    this.headers[header] shouldContain value.toString()
}

internal suspend fun HttpResponse.contains(value: CharSequence) {
    this.bodyAsText() shouldContain value.toString()
}

internal suspend fun HttpResponse.notContains(value: CharSequence) {
    this.bodyAsText() shouldNotContain value.toString()
}
