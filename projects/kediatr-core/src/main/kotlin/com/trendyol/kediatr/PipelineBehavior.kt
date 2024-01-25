package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking pipeline behavior
 *
 * @since 1.0.12
 */
interface PipelineBehavior {
    /**
     * Process to invoke before handling any query, command or notification
     *
     * @param request the request to handle
     * @param next the represents the CommandHandler handle function
     */
    suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: RequestHandlerDelegate<TRequest, TResponse>
    ): TResponse
}
