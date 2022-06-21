package com.turbomates.testsupport

import com.turbomates.testsupport.exposed.testDatabase
import databuilders.UserMother
import databuilders.UserTable
import integrationTest
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import org.jetbrains.exposed.sql.SchemaUtils
import org.junit.jupiter.api.Test

class DatabaseTest {
    @Test
    fun `test database extensions`() = integrationTest {
        shouldNotThrow<Throwable> {
            SchemaUtils.create(UserTable)
            val user = testDatabase has UserMother.hasUser()
            UserTable.assertCount(1)
            UserTable.assertCount(1) { UserTable.name eq user.builder.name }
            UserTable.assertCount(0) { UserTable.name eq "wrong name" }
            UserTable.hasValue(user.builder.name, UserTable.name)
            UserTable.doesNotHaveValue("wrong name", UserTable.name)
        }
    }

    @Test
    fun `test hasValue throws assertion error`() = integrationTest {
        shouldThrow<AssertionError> {
            SchemaUtils.create(UserTable)
            testDatabase has UserMother.hasUser()
            UserTable.hasValue("wrong name", UserTable.name)
        }
    }

    @Test
    fun `test doesNotHaveValue throws assertion error`() = integrationTest {
        shouldThrow<AssertionError> {
            SchemaUtils.create(UserTable)
            val user = testDatabase has UserMother.hasUser()
            UserTable.doesNotHaveValue(user.builder.name, UserTable.name)
        }
    }

    @Test
    fun `test assertCount throws assertion error`() = integrationTest {
        shouldThrow<AssertionError> {
            SchemaUtils.create(UserTable)
            testDatabase has UserMother.hasUser()
            UserTable.assertCount(0)
        }
    }
}
