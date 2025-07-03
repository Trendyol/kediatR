package com.trendyol.kediatr

/**
 * Default implementation of the Mediator interface.
 *
 * This implementation handles the routing of queries, commands, and notifications
 * to their respective handlers while applying pipeline behaviors for cross-cutting concerns.
 * The pipeline behaviors are executed in order based on their precedence values.
 *
 * @param registry The registry used to resolve handlers and pipeline behaviors
 * @see Mediator
 * @see Registry
 * @see PipelineBehavior
 */
class MediatorImpl(
  private val registry: Registry
) : Mediator {
  /**
   * Pipeline behaviors sorted by their order in descending order.
   * Behaviors with lower order values (higher precedence) are executed first.
   */
  private val sortedPipelineBehaviors by lazy { registry.getPipelineBehaviors().sortedByDescending { it.order } }

  /**
   * Sends a query through the pipeline and returns the response.
   *
   * @param TQuery The type of query that extends Query<TResponse>
   * @param TResponse The type of response that the query handler will return
   * @param query The query instance to send
   * @return The response from the query handler
   */
  override suspend fun <TQuery : Query<TResponse>, TResponse> send(
    query: TQuery
  ): TResponse = handle(query) { registry.resolveQueryHandler(query.javaClass).handle(query) }

  /**
   * Sends a command through the pipeline and returns the result.
   *
   * @param TCommand The type of command that extends Command<TResult>
   * @param TResult The type of result that the command handler will return
   * @param command The command instance to send
   * @return The result from the command handler
   */
  override suspend fun <TCommand : Command<TResult>, TResult> send(
    command: TCommand
  ): TResult = handle(command) { registry.resolveCommandHandler(command.javaClass).handle(command) }

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
  ) = handle(notification) { publishStrategy.publish(notification, registry.resolveNotificationHandlers(notification.javaClass)) }

  /**
   * Executes the request through the pipeline of behaviors.
   *
   * This method builds a chain of pipeline behaviors around the final handler,
   * allowing each behavior to execute logic before and after the request processing.
   *
   * @param TRequest The type of request being handled
   * @param TResponse The type of response being returned
   * @param request The request instance to handle
   * @param handler The final handler delegate that processes the request
   * @return The response from the pipeline execution
   */
  private suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    handler: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = sortedPipelineBehaviors
    .fold(handler) { next, pipeline -> { pipeline.handle(request) { next(it) } } }(request)
}
