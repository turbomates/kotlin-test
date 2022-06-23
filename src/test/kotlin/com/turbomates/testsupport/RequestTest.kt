package com.turbomates.testsupport

import com.turbomates.testsupport.response.assertIsOk
import integrationTest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.ContentType.Application.Json
import org.junit.jupiter.api.Test

class RequestTest {
    @Test
    fun `get`() = integrationTest {
        get("/api/users") {}.assertIsOk()
    }

    @Test
    fun `json post`() = integrationTest {
        jsonRequest.post("/api/users") {}.assertIsOk()
    }

    @Test
    fun `json delete`() = integrationTest {
        delete("/api/users") {}.assertIsOk()
    }

    @Test
    fun `json delete with body`() = integrationTest {
        Json.deleteWithBody("/api/users") {}.assertIsOk()
    }
}

val jsonRequest: HttpRequestBuilder.() -> Unit = {
    header("Content-Type", Json.contentType)
}
