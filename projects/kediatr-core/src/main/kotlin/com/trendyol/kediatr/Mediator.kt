package com.trendyol.kediatr

interface Mediator {
  suspend fun <TQuery : Query<TResponse>, TResponse> send(query: TQuery): TResponse

  suspend fun <TCommand : Command> send(command: TCommand)

  suspend fun <TCommand : CommandWithResult<TResult>, TResult> send(command: TCommand): TResult

  /**
   * Publishes the given notification to appropriate notification handlers
   *
   * @since 1.0.9
   * @param T  any [Notification] subclass to publish
   */
  suspend fun <T : Notification> publish(notification: T)

  suspend fun <T : Notification> publish(
    notification: T,
    publishStrategy: PublishStrategy
  )
}
