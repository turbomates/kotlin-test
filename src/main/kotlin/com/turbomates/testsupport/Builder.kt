package com.turbomates.testsupport

import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.dao.Entity

interface Builder<E> {
    fun build(): E
}

interface RequestSerializable<B : Builder<*>> {
    fun toRequest(): JsonElement
}

interface ResponseSerializable<B : Builder<*>> {
    fun toResponse(): JsonElement
}

interface DbAssertive<T : Entity<*>> {
    fun seeInDb()
}

infix fun <U, V : Builder<U>> V.with(block: (V.() -> Unit)) = apply { block.invoke(this) }
