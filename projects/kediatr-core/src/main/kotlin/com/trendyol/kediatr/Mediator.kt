package com.trendyol.kediatr

/**
 * Mediator interface for sending queries, commands, and notifications.
 *
 * The Mediator pattern encapsulates how objects interact with each other, promoting loose coupling
 * by preventing objects from referring to each other explicitly. This implementation provides
 * a centralized way to handle:
 * - Queries: Request-response operations that return data
 * - Commands: Operations that modify state and may return results
 * - Notifications: Fire-and-forget messages that can have multiple handlers
 *
 * @see Query
 * @see Command
 * @see Notification
 * @see PublishStrategy
 */
interface Mediator {
  /**
   * Sends a query and returns the response.
   *
   * Queries are typically used for read operations that return data without side effects.
   * Each query type should have exactly one handler that processes it.
   *
   * @param TQuery The type of query that extends Query<TResponse>
   * @param TResponse The type of response that the query handler will return
   * @param query The query instance to send
   * @return The response of the query as returned by its handler
   * @throws IllegalStateException if no handler is found for the query type
   * @throws Exception any exception thrown by the query handler
   */
  suspend fun <TQuery : Query<TResponse>, TResponse> send(query: TQuery): TResponse

  /**
   * Sends a command and returns the result.
   *
   * Commands are typically used for write operations that modify state.
   * Each command type should have exactly one handler that processes it.
   *
   * @param TCommand The type of command that extends Command<TResult>
   * @param TResult The type of result that the command handler will return
   * @param command The command instance to send
   * @return The result of the command as returned by its handler
   * @throws IllegalStateException if no handler is found for the command type
   * @throws Exception any exception thrown by the command handler
   */
  suspend fun <TCommand : Command<TResult>, TResult> send(command: TCommand): TResult

  /**
   * Publishes a notification using the specified publish strategy.
   *
   * Notifications are fire-and-forget messages that can have zero, one, or multiple handlers.
   * The publish strategy determines how multiple handlers are executed (sequentially, in parallel, etc.)
   * and how exceptions are handled.
   *
   * @param T The type of notification that extends Notification
   * @param notification The notification instance to publish
   * @param publishStrategy The strategy to use for publishing the notification.
   *                       Defaults to [PublishStrategy.DEFAULT] which stops on first exception.
   * @throws Exception depending on the publish strategy:
   *                   - DEFAULT/StopOnException: throws the first exception encountered
   *                   - ContinueOnException: throws AggregateException if any handlers failed
   *                   - Parallel strategies: may throw exceptions from handlers
   *
   * @see PublishStrategy.DEFAULT
   * @see PublishStrategy.CONTINUE_ON_EXCEPTION
   * @see PublishStrategy.PARALLEL_NO_WAIT
   * @see PublishStrategy.PARALLEL_WHEN_ALL
   */
  suspend fun <T : Notification> publish(notification: T, publishStrategy: PublishStrategy = PublishStrategy.DEFAULT)

  companion object {
    /**
     * Creates a new Mediator instance with the provided dependency provider.
     *
     * The dependency provider is used to resolve handlers for queries, commands, and notifications.
     * It should be configured to provide instances of:
     * - QueryHandler implementations for each query type
     * - CommandHandler implementations for each command type
     * - NotificationHandler implementations for each notification type
     *
     * @param dependencyProvider The dependency provider that will resolve handler instances
     * @return A configured Mediator instance ready for use
     *
     * @see DependencyProvider
     * @see QueryHandler
     * @see CommandHandler
     * @see NotificationHandler
     */
    fun build(dependencyProvider: DependencyProvider): Mediator = MediatorImpl(RegistryImpl(dependencyProvider))
  }
}
