package com.trendyol.kediatr.common

import com.trendyol.kediatr.AsyncQueryHandler
import com.trendyol.kediatr.DependencyProvider
import com.trendyol.kediatr.QueryHandler

/**
 * QueryProvider creates a query handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class QueryProvider<H : QueryHandler<*, *>>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}

/**
 * QueryProvider creates a async query handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class AsyncQueryProvider<H : AsyncQueryHandler<*, *>>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}