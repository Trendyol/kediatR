package com.trendyol.kediatr

/**
 * QueryProvider creates a async query handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class AsyncQueryProvider<H : AsyncQueryHandler<*, *>>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>,
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}
