package com.turbomates.testsupport.exposed

import io.kotest.assertions.errorCollectorContextElement
import java.sql.Connection
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.statements.api.ExposedSavepoint
import org.jetbrains.exposed.sql.transactions.TransactionInterface
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transactionManager

class TransactionManager(
    private val db: Database,
    @Volatile override var defaultIsolationLevel: Int = db.config.defaultIsolationLevel,
    @Volatile override var defaultRepetitionAttempts: Int = db.config.defaultRepetitionAttempts,
) : TransactionManager {
    private val threadTransactions = mutableMapOf<Thread, Transaction>()
    private var wrapperTransaction: Transaction? = null
    private var inClosingState: Boolean = false
    override var defaultReadOnly = db.config.defaultReadOnly

    override fun bindTransactionToThread(transaction: Transaction?) {

        if (!inClosingState) {
            if (transaction !is TestTransaction && transaction != null) {
                this.threadTransactions[Thread.currentThread()] = transaction
            } else {
                this.threadTransactions.remove(Thread.currentThread())
            }
        }
    }

    override fun currentOrNull(): Transaction? {
        val current = threadTransactions[Thread.currentThread()]
        return if (!inClosingState) {
            current
        } else {
            wrapperTransaction
        }
    }

    override fun newTransaction(isolation: Int, readOnly: Boolean, outerTransaction: Transaction?): Transaction {
        check(!inClosingState) {
            "You are trying to create new transaction outside of rollbackTransaction"
        }

        val parent: Transaction = outerTransaction ?: currentOrNull() ?: wrapperTransaction!!
        val newTransaction = Transaction(
            TestTransactionImpl(
                db = db,
                readOnly = outerTransaction?.readOnly ?: readOnly,
                transactionIsolation = defaultIsolationLevel,
                manager = this,
                outerTransaction = parent
            )
        )
        bindTransactionToThread(newTransaction)

        return newTransaction
    }

    fun prepareWrapperTransaction(): TestTransaction = synchronized(this) {
        check(wrapperTransaction == null) {
            "trying to open new wrapper transaction while old is not closed yet"
        }
        inClosingState = false
        threadTransactions.clear()
        wrapperTransaction = object : TestTransaction, Transaction(
            TestTransactionImpl(
                db = db,
                readOnly = db.config.defaultReadOnly,
                transactionIsolation = defaultIsolationLevel,
                manager = this@TransactionManager,
                outerTransaction = null
            )
        ) {}
        return wrapperTransaction as TestTransaction
    }

    fun rollback() = synchronized(this) {
        inClosingState = true
        threadTransactions.forEach { it.value.rollbackSafely() }
        wrapperTransaction?.rollbackSafely()
        wrapperTransaction = null
        threadTransactions.clear()
    }

    private fun Transaction.rollbackSafely() {
        try {
            rollback()
        } catch (transient: Exception) {
        }
        closeStatementsAndConnection(this)
    }

    private class TestTransactionImpl(
        override val db: Database,
        override val readOnly: Boolean,
        override val transactionIsolation: Int,
        val manager: TransactionManager,
        override val outerTransaction: Transaction?
    ) : TransactionInterface {
        private val id = UUID.randomUUID()

        override val connection = outerTransaction?.connection ?: db.connector().apply {
            transactionIsolation = this@TestTransactionImpl.transactionIsolation
            readOnly = this@TestTransactionImpl.readOnly
            autoCommit = false
        }

        private val useSavePoints = outerTransaction != null && db.useNestedTransactions
        private var savepoint: ExposedSavepoint? = if (useSavePoints) {
            connection.setSavepoint(savepointName)
        } else null

        override fun commit() {
            if (!useSavePoints) {
                connection.commit()
            }
        }

        override fun rollback() {
            if (!connection.isClosed) {
                if (useSavePoints && savepoint != null) {
                    connection.rollback(savepoint!!)
                    savepoint = connection.setSavepoint(savepointName)
                } else {
                    connection.rollback()
                }
            }
        }

        override fun close() {
            try {
                if (!useSavePoints) {
                    connection.close()
                } else {
                    savepoint?.let {
                        connection.releaseSavepoint(it)
                        savepoint = null
                    }
                }
            } finally {
                manager.bindTransactionToThread(outerTransaction)
            }
        }

        private val savepointName: String
            get() {
                var nestedLevel = 0
                var currentTransaction = outerTransaction
                while (currentTransaction != null) {
                    nestedLevel++
                    currentTransaction = currentTransaction.outerTransaction
                }
                return "Exposed_${id}_savepoint_$nestedLevel"
            }
    }
}

// NOTE: copy-pasted from ThreadLocalTransactionManager
internal fun closeStatementsAndConnection(transaction: Transaction) {
    val currentStatement = transaction.currentStatement

    try {
        currentStatement?.let {
            it.closeIfPossible()
            transaction.currentStatement = null
        }
        transaction.closeExecutedStatements()
    } catch (e: Exception) {
        exposedLogger.warn("Statements close failed", e)
    }
    try {
        transaction.close()
    } catch (e: Exception) {
        exposedLogger.warn("Transaction close failed: ${e.message}. Statement: $currentStatement", e)
    }
}

interface TestTransaction
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
        },
        manager = { database ->
            TransactionManager(
                database,
                Connection.TRANSACTION_READ_COMMITTED,
            )
        }
    )
}

fun rollbackTransaction(statement: suspend TestTransaction.() -> Unit) = runTest {
    val manager = (testDatabase.transactionManager as com.turbomates.testsupport.exposed.TransactionManager)
    val testTransaction = manager.prepareWrapperTransaction()
    try {
        testTransaction.statement()
    } finally {
        manager.rollback()
    }
}

fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking(errorCollectorContextElement, test)
