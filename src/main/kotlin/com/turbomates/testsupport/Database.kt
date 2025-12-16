@file:Suppress("ForbiddenImport")

package com.turbomates.testsupport

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldNotBeIn
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.withTransactionContext

@OptIn(InternalApi::class)
context(transaction: JdbcTransaction)
suspend infix fun <E, B : Builder<E>> Database.has(builder: B): B =
    withTransactionContext(transaction) {
        suspendTransaction {
            val entity = builder.build()
            builder.entity = entity
            builder
        }
    }

@OptIn(InternalApi::class)
suspend fun JdbcTransaction.transaction(block: suspend JdbcTransaction.() -> Unit) {
    withTransactionContext(this) {
        suspendTransaction {
            block()
        }
    }
}

fun IdTable<*>.assertCount(count: Int, where: () -> Op<Boolean> = { Op.TRUE }) {
    val countInDatabase = this.selectAll().where { where() }.map { it[this.id] }.count()
    countInDatabase shouldBe count
}

fun Table.assertCount(count: Long, where: () -> Op<Boolean> = { Op.TRUE }) {
    val countInDatabase = this.selectAll().where { where() }.count()
    countInDatabase shouldBe count
}

fun <T> Table.hasValue(value: T, column: Column<T>) {
    val result = this.selectAll().map { it[column] }
    value shouldBeIn result
}

fun <T> Table.doesNotHaveValue(value: T, column: Column<T>) {
    val result = this.selectAll().map { it[column] }
    value shouldNotBeIn result
}

fun Table.assertIsEmpty() {
    this.selectAll().toList() shouldBe emptyList()
}
