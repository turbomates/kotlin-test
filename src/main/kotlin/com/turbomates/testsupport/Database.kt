@file:Suppress("ForbiddenImport")

package com.turbomates.testsupport

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldNotBeIn
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

infix fun <E, B : Builder<E>> Database.has(builder: B): B =
    transaction(this) {
        val entity = builder.build()
        builder.entity = entity
        builder
    }

fun IdTable<*>.assertCount(count: Int, where: SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }) {
    val countInDatabase = this.select { where() }.map { it[this.id] }.count()
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
