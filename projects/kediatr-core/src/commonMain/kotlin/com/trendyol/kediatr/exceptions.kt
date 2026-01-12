package com.trendyol.kediatr

import kotlin.reflect.KClass

/**
 * Exception thrown when no handler is found for a specific request type.
 *
 * This exception is typically thrown by the Registry when attempting to resolve
 * a handler for a request type that has no registered handler implementation.
 * It indicates a configuration issue where the dependency provider is not configured
 * to provide the required handler.
 *
 * @param message Descriptive message about which handler was not found
 * @see Registry.resolveHandler
 * @see DependencyProvider
 */
class HandlerNotFoundException(
  val requestType: KClass<*>,
  val availableHandlers: List<KClass<*>>
) : Exception() {
  override val message: String
    get() = buildString {
      appendLine("Handler not found for ${requestType.simpleName}")
      appendLine("Available handlers: ${availableHandlers.map { it.simpleName }}")
    }
}

/**
 * A container for one or more exceptions that occurred during multiple task execution.
 *
 * This exception is thrown by the [PublishStrategy.ContinueOnExceptionPublishStrategy] when one or more
 * notification handlers fail during processing. It aggregates all the individual exceptions
 * that occurred, allowing the caller to examine each failure while still maintaining
 * the exception flow.
 *
 * Example usage:
 * ```kotlin
 * try {
 *     mediator.publish(notification, PublishStrategy.CONTINUE_ON_EXCEPTION)
 * } catch (e: AggregateException) {
 *     e.exceptions.forEach { exception ->
 *         logger.error("Handler failed", exception)
 *     }
 * }
 * ```
 *
 * @param exceptions Collection of exceptions that occurred during execution
 * @see PublishStrategy.ContinueOnExceptionPublishStrategy
 */
class AggregateException(
  /**
   * The collection of exceptions that were aggregated.
   * Contains all the individual exceptions that occurred during the operation.
   */
  val exceptions: Iterable<Throwable>
) : RuntimeException()
