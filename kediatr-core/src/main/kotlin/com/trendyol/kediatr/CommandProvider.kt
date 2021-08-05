package com.trendyol.kediatr.common

import com.trendyol.kediatr.AsyncCommandHandler
import com.trendyol.kediatr.AsyncCommandWithResultHandler
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.DependencyProvider
import com.trendyol.kediatr.CommandWithResultHandler

/**
 * CommandProvider creates a command handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class CommandProvider<H : CommandHandler<*>>(private val dependencyProvider: DependencyProvider, private val type: Class<H>) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}

/**
 * CommandProvider creates a command handler with enabled spring injection.
 *
 * @since 1.0.16
 * @param <H> type of handler
</H> */
internal class CommandWithResultProvider<H : CommandWithResultHandler<*, *>>(private val applicationContext: ApplicationContext, private val type: Class<H>) {

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

/**
 * CommandProvider creates a async command handler with enabled spring injection.
 *
 * @since 1.0.16
 * @param <H> type of handler
</H> */
internal class AsyncCommandWithResultProvider<H : AsyncCommandWithResultHandler<*, *>>(private val applicationContext: ApplicationContext, private val type: Class<H>) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}