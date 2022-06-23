package com.turbomates.testsupport.response

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

internal fun JsonArray.arrayContains(value: JsonElement) {
    this shouldContain value
}

internal fun JsonArray.hasCount(count: Int) {
    count() shouldBe count
}

internal suspend inline fun <reified T : JsonElement> HttpResponse.toJsonElement(): T {
    return Json.decodeFromString(bodyAsText())
}

@Suppress("unchecked_cast")
internal suspend inline fun <reified T> HttpResponse.mapTo(): T {
    return Json.decodeFromString(serializer(), bodyAsText())
}
