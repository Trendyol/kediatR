package com.trendyol.kediatr

class MediatorImpl(
  private val registry: Registry,
  private val defaultPublishStrategy: PublishStrategy = StopOnExceptionPublishStrategy()
) : Mediator {
  private val sortedPipelineBehaviors by lazy { registry.getPipelineBehaviors().sortedByDescending { it.order } }

  override suspend fun <TQuery : Query<TResponse>, TResponse> send(
    query: TQuery
  ): TResponse = handle(query) { registry.resolveQueryHandler(query.javaClass).handle(query) }

  override suspend fun <TCommand : Command> send(
    command: TCommand
  ) = handle(command) { registry.resolveCommandHandler(command.javaClass).handle(command) }

  override suspend fun <TCommand : CommandWithResult<TResult>, TResult> send(
    command: TCommand
  ): TResult = handle(command) { registry.resolveCommandWithResultHandler(command.javaClass).handle(command) }

  override suspend fun <T : Notification> publish(notification: T) = publish(notification, defaultPublishStrategy)

  override suspend fun <T : Notification> publish(
    notification: T,
    publishStrategy: PublishStrategy
  ) = handle(notification) { publishStrategy.publish(notification, registry.resolveNotificationHandlers(notification.javaClass)) }

  private suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    handler: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = sortedPipelineBehaviors
    .fold(handler) { next, pipeline -> { pipeline.handle(request) { next(it) } } }(request)
}
