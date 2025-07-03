package com.trendyol.kediatr

/**
 * Registry interface for resolving handlers and pipeline behaviors.
 *
 * The Registry acts as a central component that uses the dependency provider
 * to resolve and cache handler instances. It provides methods to find the
 * appropriate handlers for queries, commands, and notifications, as well as
 * any pipeline behaviors that should be applied.
 *
 * @see DependencyProvider
 * @see CommandHandler
 * @see QueryHandler
 * @see NotificationHandler
 * @see PipelineBehavior
 */
interface Registry {
  /**
   * Resolves the command handler for the specified command type.
   *
   * Each command type should have exactly one handler. This method will throw
   * an exception if no handler or multiple handlers are found.
   *
   * @param TCommand The type of command that extends Command<TResult>
   * @param TResult The type of result that the command handler will return
   * @param classOfCommand The class object representing the command type
   * @return The command handler instance for the specified command type
   * @throws HandlerNotFoundException if no handler is found for the command type
   * @throws IllegalStateException if multiple handlers are found for the command type
   */
  fun <TCommand : Command<TResult>, TResult> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand, TResult>

  /**
   * Resolves the query handler for the specified query type.
   *
   * Each query type should have exactly one handler. This method will throw
   * an exception if no handler or multiple handlers are found.
   *
   * @param TQuery The type of query that extends Query<TResult>
   * @param TResult The type of result that the query handler will return
   * @param classOfQuery The class object representing the query type
   * @return The query handler instance for the specified query type
   * @throws HandlerNotFoundException if no handler is found for the query type
   * @throws IllegalStateException if multiple handlers are found for the query type
   */
  fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery, TResult>

  /**
   * Resolves all notification handlers for the specified notification type.
   *
   * Unlike queries and commands, notifications can have zero, one, or multiple handlers.
   * This method returns all handlers that can process the given notification type.
   *
   * @param TNotification The type of notification that extends Notification
   * @param classOfNotification The class object representing the notification type
   * @return A collection of all notification handlers for the specified notification type
   */
  fun <TNotification : Notification> resolveNotificationHandlers(
    classOfNotification: Class<TNotification>
  ): Collection<NotificationHandler<TNotification>>

  /**
   * Gets all registered pipeline behaviors.
   *
   * Pipeline behaviors are cross-cutting concerns that can be applied to all
   * requests (queries and commands) to provide functionality like logging,
   * validation, caching, transaction management, etc.
   *
   * @return A collection of all registered pipeline behaviors
   * @see PipelineBehavior
   */
  fun getPipelineBehaviors(): Collection<PipelineBehavior>
}
