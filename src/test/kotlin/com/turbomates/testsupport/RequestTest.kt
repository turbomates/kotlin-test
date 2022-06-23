package com.turbomates.testsupport

import com.turbomates.testsupport.response.assertIsOk
import integrationTest
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
        jsonRequest.deleteWithBody("/api/users") {}.assertIsOk()
    }
}
