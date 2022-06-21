@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.turbomates.testsupport

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

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

internal fun JsonArray.arrayContains(value: JsonElement) {
    this shouldContain value
}

internal fun JsonArray.hasCount(count: Int) {
    count() shouldBe count
}

internal suspend fun HttpResponse.contains(value: CharSequence) {
    this.bodyAsText() shouldContain value.toString()
}

internal suspend fun HttpResponse.notContains(value: CharSequence) {
    this.bodyAsText() shouldNotContain value.toString()
}

internal suspend inline fun <reified T : JsonElement> HttpResponse.toJsonElement(): T {
    return Json.decodeFromString(bodyAsText())
}

@Suppress("unchecked_cast")
internal suspend inline fun <reified T> HttpResponse.mapTo(): T {
    return Json.decodeFromString(serializer(), bodyAsText())
}
