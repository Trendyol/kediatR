package com.trendyol.kediatr

/**
 * Default implementation of the Mediator interface.
 *
 * This implementation handles the routing of requests and notifications
 * to their respective handlers while applying pipeline behaviors for cross-cutting concerns.
 * The pipeline behaviors are executed in order based on their precedence values.
 *
 * @param registry The registry used to resolve handlers and pipeline behaviors
 * @see Mediator
 * @see Registry
 * @see PipelineBehavior
 */
@Suppress("UNCHECKED_CAST")
internal class MediatorImpl(
  private val registry: Registry
) : Mediator {
  /**
   * Pipeline behaviors sorted by their order in descending order.
   * Behaviors with lower order values (higher precedence) are executed first.
   */
  private val sortedPipelineBehaviors by lazy { registry.getPipelineBehaviors().sortedByDescending { it.order } }

  /**
   * Sends a request through the pipeline and returns the response.
   *
   * @param TRequest The type of request that extends Request<TResponse>
   * @param TResponse The type of response that the request handler will return
   * @return The response from the request handler
   */
  override suspend fun <TRequest : Request<TResponse>, TResponse> send(
    request: TRequest
  ): TResponse = handle(request) { registry.resolveHandler(it::class as kotlin.reflect.KClass<TRequest>).handle(it) }

  /**
   * Publishes a notification using the specified publish strategy.
   *
   * @param T The type of notification that extends Notification
   * @param notification The notification instance to publish
   * @param publishStrategy The strategy to use for publishing the notification
   */
  override suspend fun <T : Notification> publish(
    notification: T,
    publishStrategy: PublishStrategy
  ) = handle(notification) { publishStrategy.publish(notification, registry.resolveNotificationHandlers(notification::class as kotlin.reflect.KClass<T>)) }

  /**
   * Executes the message through the pipeline of behaviors.
   *
   * This method builds a chain of pipeline behaviors around the final handler,
   * allowing each behavior to execute logic before and after the message processing.
   *
   * @param TRequest The type of message being handled
   * @param TResponse The type of response being returned
   * @param request The message instance to handle
   * @param handler The final handler delegate that processes the message
   * @return The response from the pipeline execution
   */
  private suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    handler: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = sortedPipelineBehaviors
    .fold(handler) { next, pipeline -> { pipeline.handle(request) { next(it) } } }(request)
}
