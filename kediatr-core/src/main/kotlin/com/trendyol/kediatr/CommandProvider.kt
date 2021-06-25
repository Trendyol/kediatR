package com.trendyol.kediatr.common

import com.trendyol.kediatr.AsyncCommandHandler
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.DependencyProvider

/**
 * CommandProvider creates a command handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class CommandProvider<H : CommandHandler<*>>(private val dependencyProvider: DependencyProvider, private val type: Class<H>) {

    fun get(): H {
        return dependencyProvider.getTypeFor(type)
    }
}

/**
 * CommandProvider creates a async command handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class AsyncCommandProvider<H : AsyncCommandHandler<*>>(private val dependencyProvider: DependencyProvider, private val type: Class<H>) {

    fun get(): H {
        return dependencyProvider.getTypeFor(type)
    }
}