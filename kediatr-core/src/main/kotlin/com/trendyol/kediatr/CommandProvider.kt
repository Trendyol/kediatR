package com.trendyol.kediatr

/**
 * CommandProvider creates a command handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class CommandProvider<H : CommandHandler<*>>(private val dependencyProvider: DependencyProvider, private val type: Class<H>) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}

/**
 * CommandProvider creates a command handler with enabled spring injection.
 *
 * @since 1.0.16
 * @param <H> type of handler
</H> */
internal class CommandWithResultProvider<H : CommandWithResultHandler<*, *>>(private val dependencyProvider: DependencyProvider, private val type: Class<H>) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}

/**
 * CommandProvider creates a async command handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class AsyncCommandProvider<H : AsyncCommandHandler<*>>(private val dependencyProvider: DependencyProvider, private val type: Class<H>) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}

/**
 * CommandProvider creates a async command handler with enabled spring injection.
 *
 * @since 1.0.16
 * @param <H> type of handler
</H> */
internal class AsyncCommandWithResultProvider<H : AsyncCommandWithResultHandler<*, *>>(private val dependencyProvider: DependencyProvider, private val type: Class<H>) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}