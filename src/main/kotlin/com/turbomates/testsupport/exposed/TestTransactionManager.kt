package com.turbomates.testsupport.exposed

import io.kotest.matchers.errorCollectorContextElement
import javax.sql.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

abstract class TestDataSourceBase(private val ds: DataSource) : DataSource by ds
object Config {
    var databaseUrl: String = "jdbc:h2:mem:test"
    var driver: String = "org.h2.Driver"
    var user: String = "root"
    var password: String = ""
}

val testDatabase by lazy {
    Database.connect(
        Config.databaseUrl,
        user = Config.user,
        password = Config.password,
        driver = Config.driver,
        databaseConfig = DatabaseConfig {
            useNestedTransactions = true
        }
    )
}

val testThreadLocalDatabase by lazy {
    Database.connect(
        Config.databaseUrl,
        user = Config.user,
        password = Config.password,
        driver = Config.driver,
        databaseConfig = DatabaseConfig { useNestedTransactions = true },
    )
}

fun rollbackTransaction(statement: suspend JdbcTransaction.() -> Unit) = runTest {
    suspendTransaction(testDatabase) {
        try {
            statement()
        } finally {
            rollback()
        }
    }
}

fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking(errorCollectorContextElement, test)
