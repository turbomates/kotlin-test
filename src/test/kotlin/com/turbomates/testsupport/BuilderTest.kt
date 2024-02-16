package com.turbomates.testsupport

import com.turbomates.testsupport.exposed.testDatabase
import databuilders.UserMother
import databuilders.UserTable
import integrationTest
import io.kotest.assertions.throwables.shouldNotThrow
import org.jetbrains.exposed.sql.SchemaUtils
import org.junit.jupiter.api.Test

class BuilderTest {
    @Test
    fun `test builder`() = integrationTest {
        shouldNotThrow<Throwable> {
            transaction {
                SchemaUtils.create(UserTable)
                val user = testDatabase has (UserMother.one() with { name = "username"; rating = 3 })
                user.toRequest()
                user.toResponse()
                user.seeInDb()
            }
        }
    }
}
