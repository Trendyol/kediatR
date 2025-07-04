package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking pipeline behavior.
 *
 * Pipeline behaviors are cross-cutting concerns that wrap around the execution
 * of queries and commands. They can be used to implement functionality such as:
 * - Logging and auditing
 * - Validation
 * - Caching
 * - Transaction management
 * - Performance monitoring
 * - Authorization and authentication
 *
 * Behaviors are executed in order based on their precedence value, forming a pipeline
 * where each behavior can execute logic before and after the next behavior in the chain.
 *
 * @see Registry.getPipelineBehaviors
 * @see RequestHandlerDelegate
 */
interface PipelineBehavior {
  companion object {
    /**
     * Useful constant for the highest precedence value.
     *
     * Behaviors with this precedence will be executed first in the pipeline.
     *
     * @see java.lang.Integer.MIN_VALUE
     */
    const val HIGHEST_PRECEDENCE = Int.MIN_VALUE

    /**
     * Useful constant for the lowest precedence value.
     *
     * Behaviors with this precedence will be executed last in the pipeline.
     *
     * @see java.lang.Integer.MAX_VALUE
     */
    const val LOWEST_PRECEDENCE = Int.MAX_VALUE
  }

  /**
   * Get the order value of this object.
   *
   * Higher values are interpreted as lower priority. As a consequence,
   * the object with the lowest value has the highest priority and will
   * be executed first in the pipeline.
   *
   * Same order values will result in arbitrary sort positions for the
   * affected objects.
   *
   * @return the order value, defaults to [HIGHEST_PRECEDENCE]
   * @see HIGHEST_PRECEDENCE
   * @see LOWEST_PRECEDENCE
   */
  val order: Int get() = HIGHEST_PRECEDENCE

  /**
   * Process to invoke before and after handling any query or command.
   *
   * This method is called for every request that goes through the mediator.
   * The behavior can execute logic before calling the next delegate, and/or
   * after the next delegate returns. The behavior must call the next delegate
   * to continue the pipeline execution.
   *
   * Example implementation:
   * ```kotlin
   * override suspend fun <TRequest, TResponse> handle(
   *     request: TRequest,
   *     next: RequestHandlerDelegate<TRequest, TResponse>
   * ): TResponse {
   *     // Pre-processing logic
   *     println("Before handling: $request")
   *
   *     val response = next(request)
   *
   *     // Post-processing logic
   *     println("After handling: $response")
   *
   *     return response
   * }
   * ```
   *
   * @param TRequest The type of request being handled (Query or Command)
   * @param TResponse The type of response being returned
   * @param request The request instance to handle
   * @param next The delegate that represents the next step in the pipeline
   * @return The response from the pipeline execution
   * @throws Exception any exception thrown by the pipeline or handler
   */
  suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse
}
