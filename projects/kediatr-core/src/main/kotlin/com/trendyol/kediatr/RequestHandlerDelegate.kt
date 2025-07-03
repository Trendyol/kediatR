package com.trendyol.kediatr

/**
 * RequestHandlerDelegate represents a function that handles queries, commands, or notifications.
 *
 * This type alias defines the signature for handler delegates used in the pipeline behavior chain.
 * Each delegate in the pipeline receives a request and returns a response, allowing behaviors
 * to intercept and modify the request/response flow.
 *
 * The delegate is typically either:
 * - Another pipeline behavior in the chain
 * - The final handler (QueryHandler or CommandHandler) that processes the request
 *
 * @param TRequest The type of request being handled (Query, Command, or Notification)
 * @param TResponse The type of response being returned
 *
 * @see PipelineBehavior
 * @see QueryHandler
 * @see CommandHandler
 */
typealias RequestHandlerDelegate<TRequest, TResponse> = suspend (TRequest) -> TResponse
