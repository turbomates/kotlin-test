package com.turbomates.testsupport

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.client.request.post as clientPost

suspend fun ApplicationTestBuilder.get(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.get(uri) { setup() }

context(ApplicationTestBuilder)
suspend fun (HttpRequestBuilder.() -> Unit).post(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
    return client().clientPost(uri) {
        this@post.invoke(this)
        setup()
    }
}

suspend fun ApplicationTestBuilder.delete(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.delete(uri) { setup() }

context(ApplicationTestBuilder)
suspend fun (HttpRequestBuilder.() -> Unit).deleteWithBody(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
    return client().delete(uri) {
        this@deleteWithBody.invoke(this)
        setup()
    }
}

val jsonRequest: HttpRequestBuilder.() -> Unit = {
    contentType(Json)
}

private fun ApplicationTestBuilder.client() = createClient {
    install(ContentNegotiation) {
        json()
    }
}
