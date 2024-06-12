package com.turbomates.testsupport.exposed

import io.kotest.assertions.errorCollectorContextElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.statements.api.ExposedSavepoint
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionInterface
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

@Suppress("NestedScopeFunctions", "UnnecessaryLet")
class TestTransactionManager(
    private val db: Database,
    @Volatile override var defaultIsolationLevel: Int = db.config.defaultIsolationLevel,
    @Volatile override var defaultRepetitionAttempts: Int = db.config.defaultRepetitionAttempts,
    override var defaultMaxAttempts: Int = db.config.defaultMaxAttempts,
    override var defaultMaxRepetitionDelay: Long = db.config.defaultMaxRepetitionDelay,
    override var defaultMaxRetryDelay: Long = db.config.defaultMaxRetryDelay,
    override var defaultMinRepetitionDelay: Long = db.config.defaultMinRepetitionDelay,
    override var defaultMinRetryDelay: Long = db.config.defaultMinRetryDelay
) : TransactionManager {
    private val threadTransactions = mutableMapOf<Thread, Transaction>()
    private var wrapperTransaction: Transaction? = null
    private var isInClosingState: Boolean = false
    override var defaultReadOnly = db.config.defaultReadOnly

    override fun bindTransactionToThread(transaction: Transaction?) {

        if (!isInClosingState) {
            if (transaction !is TestTransaction && transaction != null) {
                this.threadTransactions[Thread.currentThread()] = transaction
            } else {
                exposedLogger.debug("remove unexpected transaction")
                this.threadTransactions.remove(Thread.currentThread())
            }
        }
    }

    override fun currentOrNull(): Transaction? {
        val current = threadTransactions[Thread.currentThread()]
        return if (!isInClosingState) {
            current
        } else {
            wrapperTransaction
        }
    }

    override fun newTransaction(isolation: Int, readOnly: Boolean, outerTransaction: Transaction?): Transaction {
        check(!isInClosingState) {
            "You are trying to create new transaction outside of rollbackTransaction"
        }

        exposedLogger.debug("open new transaction in rollback statement")
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
        exposedLogger.debug("open wrapper transaction")
        isInClosingState = false
        threadTransactions.clear()
        wrapperTransaction = object : TestTransaction, Transaction(
            TestTransactionImpl(
                db = db,
                readOnly = db.config.defaultReadOnly,
                transactionIsolation = defaultIsolationLevel,
                manager = this@TestTransactionManager,
                outerTransaction = null
            )
        ) {}
        return wrapperTransaction as TestTransaction
    }

    fun rollback() = synchronized(this) {
        exposedLogger.debug("rollback all transactions")
        isInClosingState = true
        threadTransactions.forEach { it.value.rollbackSafely() }
        wrapperTransaction?.rollbackSafely()
        wrapperTransaction = null
        threadTransactions.clear()
    }

    private fun Transaction.rollbackSafely() {
        try {
            rollback()
        } catch (_: Exception) {
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
                if (shouldUseSavePoints && savepoint != null) {
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
        },
        manager = { database ->
            TestTransactionManager(
                database,
                Connection.TRANSACTION_READ_COMMITTED,
            )
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
        manager = { database ->
            ThreadLocalTransactionManager(
                database
            )
        }
    )
}

fun rollbackTransaction(statement: suspend TestTransaction.() -> Unit) = runTest {
    val manager = (testDatabase.transactionManager as TestTransactionManager)
    val testTransaction = manager.prepareWrapperTransaction()
    try {
        testTransaction.statement()
    } finally {
        manager.rollback()
    }
}

fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking(errorCollectorContextElement, test)
