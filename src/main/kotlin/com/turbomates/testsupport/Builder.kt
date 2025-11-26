package com.turbomates.testsupport

import kotlin.properties.Delegates
import org.jetbrains.exposed.v1.dao.Entity

abstract class Builder<E : Any> {
    var entity by Delegates.notNull<E>()
    abstract fun build(): E
}

interface Fixture<B : Builder<*>> {
    fun load(block: B.() -> Unit): B
    fun rollback()
}

interface RequestSerializable<out T> {
    fun toRequest(): T
}

interface ResponseSerializable<out T> {
    fun toResponse(): T
}

interface DbAssertive<T : Entity<*>> {
    fun seeInDb()
}

infix fun <U, V : Builder<U>> V.with(block: (V.() -> Unit)) = apply { block.invoke(this) }
