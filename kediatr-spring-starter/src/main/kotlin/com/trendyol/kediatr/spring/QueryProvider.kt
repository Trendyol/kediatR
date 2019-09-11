package com.trendyol.kediatr.spring

import com.trendyol.kediatr.AsyncQueryHandler
import com.trendyol.kediatr.QueryHandler
import org.springframework.context.ApplicationContext

/**
 * QueryProvider creates a query handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class QueryProvider<H : QueryHandler<*, *>>(private val applicationContext: ApplicationContext, private val type: Class<H>) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}

/**
 * QueryProvider creates a async query handler with enabled spring injection.
 *
 * @param <H> type of handler
</H> */
internal class AsyncQueryProvider<H : AsyncQueryHandler<*, *>>(private val applicationContext: ApplicationContext, private val type: Class<H>) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}