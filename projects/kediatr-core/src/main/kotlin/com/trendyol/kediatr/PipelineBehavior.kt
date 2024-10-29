package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking pipeline behavior
 *
 */
interface PipelineBehavior {
  companion object {
    /**
     * Useful constant for the highest precedence value.
     * @see java.lang.Integer.MIN_VALUE
     */
    const val HIGHEST_PRECEDENCE = Int.MIN_VALUE

    /**
     * Useful constant for the lowest precedence value.
     * @see java.lang.Integer.MAX_VALUE
     */
    const val LOWEST_PRECEDENCE = Int.MAX_VALUE
  }

  /**
   * Get the order value of this object.
   *
   * Higher values are interpreted as lower priority. As a consequence,
   * the object with the lowest value has the highest priority.
   *
   * Same order values will result in arbitrary sort positions for the
   * affected objects.
   * @return the order value
   * @see .HIGHEST_PRECEDENCE
   *
   * @see .LOWEST_PRECEDENCE
   */
  val order: Int get() = HIGHEST_PRECEDENCE

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
