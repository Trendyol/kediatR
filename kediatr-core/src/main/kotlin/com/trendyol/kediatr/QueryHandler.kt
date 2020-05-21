package com.trendyol.kediatr

/**
 * Interface to be implemented for a query handler
 *
 * @since 1.0.0
 * @param TQuery any [Query] subclass to handle
 * @see Query
 * @see AsyncQueryHandler
 */
interface QueryHandler<TQuery : Query<TResponse>, TResponse> {
    /**
     * Handles a query
     *
     * @param query the query to handle
     */
    fun handle(query: TQuery): TResponse
}