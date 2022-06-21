package com.turbomates.testsupport.exposed

import org.jetbrains.exposed.sql.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.api.ExposedSavepoint
import org.jetbrains.exposed.sql.transactions.TransactionInterface
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class TransactionManager(
    private val db: Database,
    @Volatile override var defaultIsolationLevel: Int,
    @Volatile override var defaultRepetitionAttempts: Int
) : TransactionManager {
    var transaction: Transaction? = null

    override fun bindTransactionToThread(transaction: Transaction?) {}

    override fun currentOrNull(): Transaction? {
        return transaction
    }

    override fun newTransaction(isolation: Int, outerTransaction: Transaction?): Transaction {
        transaction = Transaction(
            TestTransaction(
                db = db,
                transactionIsolation = defaultIsolationLevel,
                manager = this,
                outerTransaction = outerTransaction ?: transaction
            )
        )
        return transaction!!
    }

    private class TestTransaction(
        override val db: Database,
        override val transactionIsolation: Int,
        val manager: com.turbomates.testsupport.exposed.TransactionManager,
        override val outerTransaction: Transaction?
    ) : TransactionInterface {
        private val savepointName: String
            get() {
                var nestedLevel = 0
                var currentTransaction = outerTransaction
                while (currentTransaction != null) {
                    nestedLevel++
                    currentTransaction = currentTransaction.outerTransaction
                }
                return "Exposed_savepoint_$nestedLevel"
            }
        override val connection = outerTransaction?.connection ?: db.connector().apply {
            autoCommit = false
            transactionIsolation = this@TestTransaction.transactionIsolation
        }

        private val shouldUseSavePoints = outerTransaction != null && db.useNestedTransactions
        private var savepoint: ExposedSavepoint? = if (shouldUseSavePoints) {
            connection.setSavepoint(savepointName)
        } else null

        override fun commit() {
            if (!shouldUseSavePoints) {
                connection.commit()
            }
        }

        override fun rollback() {
            if (!connection.isClosed) {
                if (shouldUseSavePoints) {
                    connection.rollback(savepoint!!)
                    savepoint = connection.setSavepoint(savepointName)
                } else {
                    connection.rollback()
                }
            }
        }

        override fun close() {
            try {
                if (!shouldUseSavePoints) {
                    connection.close()
                } else {
                    savepoint?.let {
                        connection.releaseSavepoint(it)
                        savepoint = null
                    }
                }
            } finally {
                manager.transaction = outerTransaction
            }
        }
    }
}

object Config {
    var databaseUrl: String = "jdbc:h2:mem:test"
    var driver: String = "org.h2.Driver"
    var user: String = "root"
    var password: String = ""
}

internal val testDatabase by lazy {
    Database.connect(
        Config.databaseUrl,
        user = Config.user,
        password = Config.password,
        driver = Config.driver,
        databaseConfig = DatabaseConfig { useNestedTransactions = true },
        manager = { database ->
            TransactionManager(
                database,
                Connection.TRANSACTION_READ_COMMITTED,
                DEFAULT_REPETITION_ATTEMPTS
            )
        }
    )
}

fun <T> rollbackTransaction(db: Database = testDatabase, statement: Transaction.() -> T): T {
    return transaction(db) { val result = statement(); rollback(); result }
}
