package com.turbomates.testsupport

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.server.testing.ApplicationTestBuilder

suspend fun ApplicationTestBuilder.handleGet(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.get(uri) { setup() }

suspend fun ApplicationTestBuilder.handlePost(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.post(uri) {
        header("Content-type", "application/json")
        setup()
    }

suspend fun ApplicationTestBuilder.handleDelete(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.delete(uri) { setup() }

suspend fun ApplicationTestBuilder.handleDeleteWithBody(uri: String, setup: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
    client.delete(uri) {
        header("Content-type", "application/json")
        setup()
    }
