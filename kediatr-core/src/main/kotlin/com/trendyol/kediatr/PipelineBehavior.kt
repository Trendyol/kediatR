package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking pipeline behavior
 *
 * @since 1.0.12
 * @see AsyncPipelineBehavior
 */
interface PipelineBehavior {
    /**
     * Process to invoke before handling any query, command or notification
     *
     * @param request the request to handle
     */
    fun <TRequest> preProcess(request: TRequest)

    /**
     * Process to invoke after handling any query, command or notification
     *
     * @param request the request to handle
     * @param response the response to handle
     */
    fun <TRequest, TResponse> postProcess(request: TRequest, response: TResponse)

    /**
     * Process to invoke after any handler encounters an exception
     *
     * @param request the request to handle
     * @param exception the exception that occurred
     */
    fun <TRequest, TException> handleExceptionProcess(request: TRequest, exception: TException) where TException : Exception
}
