package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking pipeline behavior
 *
 * @since 1.0.12
 * @see AsyncPipelineBehavior
 */
interface AsyncPipelineBehavior {
    /**
     * Process to invoke before handling any query, command or notification
     *
     * @param request the request to handle
     */
    suspend fun <TRequest>preProcess(request: TRequest)

    /**
     * Process to invoke after handling any query, command or notification
     *
     * @param request the request to handle
     */
    suspend fun <TRequest>postProcess(request: TRequest)
}