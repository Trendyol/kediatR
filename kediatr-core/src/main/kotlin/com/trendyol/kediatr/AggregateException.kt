package com.trendyol.kediatr

/**
 * A container for one or more exceptions that occurred during multiple task execution.
 *
 * @since 1.0.9
 */
class AggregateException(val exceptions: Collection<Throwable>) : RuntimeException() {
    constructor(exceptions: Array<Throwable>) : this(exceptions.toList())
}