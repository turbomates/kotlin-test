@file:Suppress("ForbiddenImport")

package com.turbomates.testsupport

import com.turbomates.testsupport.exposed.TransactionManager
import com.turbomates.testsupport.exposed.testDatabase
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldNotBeIn
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

private val transactionManager: TransactionManager by lazy {
    TransactionManager(testDatabase)
}

suspend fun <T> transaction(statement: suspend Transaction.() -> T): T =
    transactionManager.invoke(statement)

infix fun <E, B : Builder<E>> Database.has(builder: B): B =
    transaction(this) {
        val entity = builder.build()
        builder.entity = entity
        builder
    }

context(Transaction)
fun IdTable<*>.assertCount(count: Int, where: SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }) {
    val countInDatabase = this.selectAll().where { where() }.map { it[this.id] }.count()
    countInDatabase shouldBe count
}

context(Transaction)
fun Table.assertCount(count: Long, where: SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }) {
    val countInDatabase = this.selectAll().where { where() }.count()
    countInDatabase shouldBe count
}

context(Transaction)
fun <T> Table.hasValue(value: T, column: Column<T>) {
    val result = this.selectAll().map { it[column] }
    value shouldBeIn result
}

context(Transaction)
fun <T> Table.doesNotHaveValue(value: T, column: Column<T>) {
    val result = this.selectAll().map { it[column] }
    value shouldNotBeIn result
}

context(Transaction)
fun Table.assertIsEmpty() {
    this.selectAll().toList() shouldBe emptyList()
}
