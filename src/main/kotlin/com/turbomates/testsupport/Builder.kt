package com.turbomates.testsupport

import org.jetbrains.exposed.dao.Entity

interface Builder<E> {
    fun build(): E
}

interface RequestSerializable<B : Builder<*>, out T> {
    fun toRequest(): T
}

interface ResponseSerializable<B : Builder<*>, out T> {
    fun toResponse(): T
}

interface DbAssertive<T : Entity<*>> {
    fun seeInDb()
}

infix fun <U, V : Builder<U>> V.with(block: (V.() -> Unit)) = apply { block.invoke(this) }
