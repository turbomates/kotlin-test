package com.turbomates.testsupport

import UserView
import com.turbomates.testsupport.response.arrayContains
import com.turbomates.testsupport.response.assert
import com.turbomates.testsupport.response.assertIsOk
import com.turbomates.testsupport.response.contains
import com.turbomates.testsupport.response.containsHeader
import com.turbomates.testsupport.response.hasCount
import com.turbomates.testsupport.response.mapTo
import com.turbomates.testsupport.response.notContains
import com.turbomates.testsupport.response.toJsonElement
import databuilders.UserMother
import integrationTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Test

class ResponseTest {
    @Test
    fun `assertIsOk success`() = integrationTest {
        val user = UserMother.hasUser().build()

        get("/api/users/${user.id}") {
        }.assertIsOk()
    }

    @Test
    fun `assertIsOk success error`() = integrationTest {
        shouldThrow<AssertionError> {
            get("/api/admins") {
            }.assertIsOk()
        }
    }

    @Test
    fun `assert success`() = integrationTest {
        get("/api/admins") {
        }.assert(HttpStatusCode.NotFound) { }
    }

    @Test
    fun `assert error`() = integrationTest {
        shouldThrow<AssertionError> {
            get("/api/admins") {
            }.assert(HttpStatusCode.Conflict) { }
        }
    }

    @Test
    fun `containsHeader success`() = integrationTest {
        get("/api/admins") {
        }.assert(HttpStatusCode.NotFound) {
            containsHeader(HttpHeaders.ContentLength, 0)
        }
    }

    @Test
    fun `containsHeader error`() = integrationTest {
        shouldThrow<AssertionError> {
            get("/api/users") {
            }.assert(HttpStatusCode.NotFound) {
                containsHeader(HttpHeaders.ContentLength, 2)
            }
        }
    }

    @Test
    fun `contains string success`() = integrationTest {
        val user = UserMother.hasDeactivatedUser().build()

        get("/api/users/${user.id}") {
        }.assert { contains(UserView().name) }
    }

    @Test
    fun `contains string error`() = integrationTest {
        shouldThrow<AssertionError> {
            get("/api/users") {
            }.assert { contains("404literal") }
        }
    }

    @Test
    fun `not contains string success`() = integrationTest {
        val user = UserMother.hasUser().build()

        get("/api/users/${user.id}") {
        }.assert { notContains("404literal") }
    }

    @Test
    fun `not contains string error`() = integrationTest {
        shouldThrow<AssertionError> {
            get("/api/users") {
            }.assert { notContains(UserView().name) }
        }
    }

    @Test
    fun `json array has count success`() = integrationTest {
        get("/api/users") {
        }.assert { toJsonElement<JsonArray>().hasCount(2) }
    }

    @Test
    fun `json array has count error`() = integrationTest {
        shouldThrow<AssertionError> {
            get("/api/users") {
            }.assert { toJsonElement<JsonArray>().hasCount(3) }
        }
    }

    @Test
    fun `map response`() = integrationTest {
        val user = UserMother.hasUser().build()

        get("/api/users/${user.id}") {
        }.assert {
            mapTo<UserView>() shouldBe UserView()
        }
    }

    @Test
    fun `json array contains success`() = integrationTest {
        get("/api/users") {
        }.assert {
            toJsonElement<JsonArray>().arrayContains(
                json.encodeToJsonElement(UserView())
            )
        }
    }

    @Test
    fun `json array contains error`() = integrationTest {
        shouldThrow<AssertionError> {
            get("/api/users") {
            }.assert {
                toJsonElement<JsonArray>().arrayContains(
                    json.encodeToJsonElement(UserView("wrong username"))
                )
            }
        }
    }
}
