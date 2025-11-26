package com.turbomates.testsupport

import UserView
import com.turbomates.testsupport.exposed.testDatabase
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
import databuilders.UserTable
import integrationTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.core.transactions.withThreadLocalTransaction
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Test

class ResponseTest {
    @OptIn(InternalApi::class)
    @Test
    fun `assertIsOk success`() = integrationTest { application ->
        transaction(testDatabase) {
            SchemaUtils.create(UserTable)
        }
        val user = testDatabase has UserMother.one()
        application.get("/api/users/${user.id}") {
        }.assertIsOk()
    }

    @Test
    fun `assertIsOk success error`() = integrationTest { application ->
        shouldThrow<AssertionError> {
            application.get("/api/admins") {
            }.assertIsOk()
        }
    }

    @Test
    fun `assert success`() = integrationTest { application ->
        application.get("/api/admins") {
        }.assert(HttpStatusCode.NotFound) { }
    }

    @Test
    fun `assert error`() = integrationTest { application ->
        shouldThrow<AssertionError> {
            application.get("/api/admins") {
            }.assert(HttpStatusCode.Conflict) { }
        }
    }

    @Test
    fun `containsHeader success`() = integrationTest { application ->
        application.get("/api/admins") {
        }.assert(HttpStatusCode.NotFound) {
            contextOf<HttpResponse>().containsHeader(HttpHeaders.ContentLength, 0)
        }
    }

    @Test
    fun `containsHeader error`() = integrationTest { application ->
        shouldThrow<AssertionError> {
            application.get("/api/users") {
            }.assert(HttpStatusCode.NotFound) {
                contextOf<HttpResponse>().containsHeader(HttpHeaders.ContentLength, 2)
            }
        }
    }

    @Test
    fun `contains string success`() = integrationTest { application ->
        transaction {
            SchemaUtils.create(UserTable)
        }
        val user = testDatabase has UserMother.deactivatedUser()

        application.get("/api/users/${user.id}") {
        }.assert { contextOf<HttpResponse>().contains(UserView().name) }
    }

    @Test
    fun `contains string error`() = integrationTest { application ->
        shouldThrow<AssertionError> {
            application.get("/api/users") {
            }.assert { contextOf<HttpResponse>().contains("404literal") }
        }
    }

    @Test
    fun `not contains string success`() = integrationTest { application ->
        transaction {
            SchemaUtils.create(UserTable)
        }
        val user = testDatabase has UserMother.one()

        application.get("/api/users/${user.id}") {
        }.assert { contextOf<HttpResponse>().notContains("404literal") }
    }

    @Test
    fun `not contains string error`() = integrationTest { application ->
        shouldThrow<AssertionError> {
            application.get("/api/users") {
            }.assert { contextOf<HttpResponse>().notContains(UserView().name) }
        }
    }

    @Test
    fun `json array has count success`() = integrationTest { application ->
        application.get("/api/users") {
        }.assert { contextOf<HttpResponse>().toJsonElement<JsonArray>().hasCount(2) }
    }

    @Test
    fun `json array has count error`() = integrationTest { application ->
        shouldThrow<AssertionError> {
            application.get("/api/users") {
            }.assert { contextOf<HttpResponse>().toJsonElement<JsonArray>().hasCount(3) }
        }
    }

    @Test
    fun `map response`() = integrationTest { application ->
        transaction(testDatabase) {
            SchemaUtils.create(UserTable)
        }
        val user = testDatabase has UserMother.one()

        application.get("/api/users/${user.id}") {
        }.assert {
            contextOf<HttpResponse>().mapTo<UserView>() shouldBe UserView()
        }
    }

    @Test
    fun `json array contains success`() = integrationTest { application ->

        application.get("/api/users") {
        }.assert {
            contextOf<HttpResponse>().toJsonElement<JsonArray>().arrayContains(
                json.encodeToJsonElement(UserView())
            )
        }
    }

    @Test
    fun `json array contains error`() = integrationTest { application ->
        shouldThrow<AssertionError> {
            application.get("/api/users") {
            }.assert {
                contextOf<HttpResponse>().toJsonElement<JsonArray>().arrayContains(
                    json.encodeToJsonElement(UserView("wrong username"))
                )
            }
        }
    }
}
