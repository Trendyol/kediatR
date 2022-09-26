package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking pipeline behavior
 *
 * @since 1.0.12
 * @see PipelineBehavior
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

    /**
     * Process to invoke after any handler encounters an exception
     *
     * @param request the request to handle
     * @param exception the exception that occurred
     */
    suspend fun <TRequest, TException> handleException(request: TRequest, exception: TException) where TException : Exception
}
