package com.turbomates.testsupport

import com.turbomates.testsupport.exposed.testDatabase
import databuilders.UserMother
import databuilders.UserTable
import integrationTest
import io.kotest.assertions.throwables.shouldNotThrow
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.core.transactions.withThreadLocalTransaction
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.junit.jupiter.api.Test

class BuilderTest {
    @OptIn(InternalApi::class)
    @Test
    fun `test builder`() = integrationTest {
        shouldNotThrow<Throwable> {
                SchemaUtils.create(UserTable)
                val user = testDatabase has (UserMother.one() with { name = "username"; rating = 3 })
                user.toRequest()
                user.toResponse()
                user.seeInDb()
            }
    }
}
