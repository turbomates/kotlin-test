package com.turbomates.testsupport.exposed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class TransactionManager(
    private val primaryDatabase: Database,
    private val replicaDatabase: List<Database> = emptyList()
) {
    constructor(primaryDatabase: Database) : this(primaryDatabase, listOf(primaryDatabase))

    suspend operator fun <T> invoke(statement: suspend JdbcTransaction.() -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction(primaryDatabase, statement = statement)
        }


    suspend fun <T> readOnlyTransaction(statement: suspend JdbcTransaction.() -> T) =
        withContext(Dispatchers.IO) {
            suspendTransaction(
                replicaDatabase.random(),
                statement = statement
            )
        }

    fun <T> sync(statement: Transaction.() -> T): T {
        return transaction(primaryDatabase, statement = statement)
    }


    fun <T> readOnlySync(statement: Transaction.() -> T): T {
        return transaction(replicaDatabase.random(), statement = statement)
    }
}
