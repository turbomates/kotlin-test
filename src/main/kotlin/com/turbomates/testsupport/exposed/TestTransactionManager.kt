package com.turbomates.testsupport.exposed

import io.kotest.matchers.errorCollectorContextElement
import java.sql.Connection
import javax.sql.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.transactions.ThreadLocalTransactionsStack
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transactionManager

//@Suppress("NestedScopeFunctions", "UnnecessaryLet")
//class TestTransactionManager(
//    private val db: Database,
//    override var defaultMaxAttempts: Int = db.config.defaultMaxAttempts,
//    override var defaultMaxRetryDelay: Long = db.config.defaultMaxRetryDelay,
//    override var defaultMinRetryDelay: Long = db.config.defaultMinRetryDelay
//) : TransactionManager{
//    private val threadTransactions = mutableMapOf<Thread, JdbcTransaction>()
//    private var wrapperTransaction: JdbcTransaction? = null
//    private var isInClosingState: Boolean = false
//    override var defaultReadOnly = db.config.defaultReadOnly
//
//    fun bindTransactionToThread(transaction: JdbcTransaction?) {
//
//        if (!isInClosingState) {
//            if (transaction !is TestTransaction && transaction != null) {
//                this.threadTransactions[Thread.currentThread()] = transaction
//            } else {
//                this.threadTransactions.remove(Thread.currentThread())
//            }
//        }
//    }
//
//    override fun currentOrNull(): Transaction? {
//        val current = threadTransactions[Thread.currentThread()]
//        return if (!isInClosingState) {
//            current
//        } else {
//            wrapperTransaction
//        }
//    }
//
//    fun newTransaction(isolation: Int, readOnly: Boolean, outerTransaction: Transaction?): Transaction {
//        check(!isInClosingState) {
//            "You are trying to create new transaction outside of rollbackTransaction"
//        }
//
//        val parent: Transaction = outerTransaction ?: currentOrNull() ?: wrapperTransaction!!
//        val newTransaction = Transaction(
//            JdbcTransaction(
//                db = db,
//                readOnly = outerTransaction?.readOnly ?: readOnly,
//                transactionIsolation = defaultIsolationLevel,
//                manager = this,
//                outerTransaction = parent
//            )
//        )
//        bindTransactionToThread(newTransaction)
//
//        return newTransaction
//    }
//
//    fun prepareWrapperTransaction(): TestTransaction = synchronized(this) {
//        check(wrapperTransaction == null) {
//            "trying to open new wrapper transaction while old is not closed yet"
//        }
//        isInClosingState = false
//        threadTransactions.clear()
//        wrapperTransaction = object : TestTransaction, Transaction(
//            TestTransactionImpl(
//                db = db,
//                readOnly = db.config.defaultReadOnly,
//                transactionIsolation = defaultIsolationLevel,
//                manager = this@TestTransactionManager,
//                outerTransaction = null
//            )
//        ) {}
//        return wrapperTransaction as TestTransaction
//    }
//
//    fun rollback() = synchronized(this) {
//        isInClosingState = true
//        threadTransactions.forEach { it.value.rollbackSafely() }
//        wrapperTransaction?.rollbackSafely()
//        wrapperTransaction = null
//        threadTransactions.clear()
//    }
//
//    private fun Transaction.rollbackSafely() {
//        try {
//            rollback()
//        } catch (_: Exception) {
//        }
//        closeStatementsAndConnection(this)
//    }
//
//
//
//// NOTE: copy-pasted from ThreadLocalTransactionManager
//internal fun closeStatementsAndConnection(transaction: JdbcTransaction) {
//    val currentStatement = transaction.currentStatement
//
//    try {
//        currentStatement?.let {
//            it.closeIfPossible()
//            transaction.currentStatement = null
//        }
//        transaction.closeExecutedStatements()
//    } catch (e: Exception) {
//    }
//    try {
//        transaction.close()
//    } catch (e: Exception) {
//    }
//}

//interface TestTransaction
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
