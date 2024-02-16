package com.turbomates.testsupport

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.serializer

fun JsonObject.obj(key: String): JsonObject {
    return this[key] as JsonObject
}

fun JsonObject.primitive(key: String): JsonPrimitive {
    return this[key] as JsonPrimitive
}
fun List<Any>.toJsonArray(): JsonArray =
    buildJsonArray { forEach { add(JsonPrimitive(it.toString())) } }

inline fun <reified T : Any> Json.toJsonElement(value: T?): JsonElement {
    if (value == null) return JsonNull
    return encodeToJsonElement(T::class.serializer(), value)
}

fun JsonObject.array(key: String): JsonArray {
    return this[key] as JsonArray
}

