package com.turbomates.testsupport

import org.jetbrains.exposed.dao.Entity
import kotlin.properties.Delegates

abstract class Builder<E : Any> {
    var entity by Delegates.notNull<E>()
    abstract fun build(): E
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
