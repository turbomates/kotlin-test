package com.turbomates.testsupport

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.server.testing.ApplicationTestBuilder

suspend fun ApplicationTestBuilder.get(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.get(uri) { setup() }

context(ApplicationTestBuilder)
suspend fun ContentType.post(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
    val contentType = toString()
    return client.post(uri) {
        header("Content-type", contentType)
        setup()
    }
}

suspend fun ApplicationTestBuilder.delete(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.delete(uri) { setup() }

context(ApplicationTestBuilder)
suspend fun ContentType.deleteWithBody(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
    val contentType = toString()
    return client.delete(uri) {
        header("Content-type", contentType)
        setup()
    }
}
