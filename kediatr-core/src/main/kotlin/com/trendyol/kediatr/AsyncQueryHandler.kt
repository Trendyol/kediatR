package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking query handler
 *
 * @since 1.0.0
 * @param TQuery any [Query] subclass to handle
 * @see Query
 * @see QueryHandler
 */
interface AsyncQueryHandler<TQuery : Query<TResponse>, TResponse> {
    /**
     * Handles a query
     *
     * @param query the query to handle
     */
    suspend fun handleAsync(query: TQuery): TResponse
}