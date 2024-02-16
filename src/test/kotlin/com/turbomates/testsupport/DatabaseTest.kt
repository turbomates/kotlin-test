package com.turbomates.testsupport

import databuilders.UserFixture
import databuilders.UserTable
import integrationTest
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class DatabaseTest {
    @Test
    fun `test database extensions`() = integrationTest {
        shouldNotThrow<AssertionError> {
            transaction {
                val user = UserFixture.load { }
                UserTable.assertCount(1)
                UserTable.assertCount(1) { UserTable.name eq user.name }
                UserTable.assertCount(0) { UserTable.name eq "wrong name" }
                UserTable.hasValue(user.name, UserTable.name)
                UserTable.doesNotHaveValue("wrong name", UserTable.name)
            }
        }
    }

    @Test
    fun `test hasValue throws assertion error`() = integrationTest {
        shouldThrow<AssertionError> {
            transaction {
                UserFixture.load { }
                UserTable.hasValue("wrong name", UserTable.name)
            }
        }
    }

    @Test
    fun `test doesNotHaveValue throws assertion error`() = integrationTest {
        shouldThrow<AssertionError> {
            transaction {
                val user = UserFixture.load { }
                UserTable.doesNotHaveValue(user.name, UserTable.name)
            }
        }
    }

    @Test
    fun `test assertCount throws assertion error`() = integrationTest {
        shouldThrow<AssertionError> {
            transaction {
                UserFixture.load { }
                UserTable.assertCount(0)
            }
        }
    }
}
