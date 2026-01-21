package com.trendyol.kediatr

/**
 * Mediator interface for sending requests and publishing notifications.
 *
 * The Mediator pattern encapsulates how objects interact with each other, promoting loose coupling
 * by preventing objects from referring to each other explicitly. This implementation provides
 * a centralized way to handle:
 * - Requests: Unified interface for both queries and commands that return responses
 * - Notifications: Fire-and-forget messages that can have multiple handlers
 *
 * @see Request
 * @see Notification
 * @see PublishStrategy
 */
interface Mediator {
  /**
   * Sends a request and returns the response.
   *
   * Requests represent both queries and commands in a unified way. Each request type
   * should have exactly one handler that processes it. Requests are typically used for:
   * - Queries: Read operations that return data without side effects
   * - Commands: Operations that modify state and may return results
   *
   * @param TResponse The type of response that the request handler will return
   * @param TRequest The type of request that extends Request<TResponse>
   * @return The response of the request as returned by its handler
   * @throws HandlerNotFoundException if no handler is found for the request type
   * @throws Exception any exception thrown by the request handler
   */
  suspend fun <TRequest : Request<TResponse>, TResponse> send(request: TRequest): TResponse

  /**
   * Publishes a notification using the specified publish strategy.
   *
   * Notifications are fire-and-forget messages that can have zero, one, or multiple handlers.
   * The publishing strategy determines how multiple handlers are executed (sequentially, in parallel, etc.)
   * and how exceptions are handled.
   *
   * @param T The type of notification that extends Notification
   * @param notification The notification instance to publish
   * @param publishStrategy The strategy to use for publishing the notification.
   *                       Defaults to [PublishStrategy.DEFAULT] which stops on first exception.
   * @throws Exception depending on the publishing strategy:
   *                   - DEFAULT/StopOnException: throws the first exception encountered
   *                   - ContinueOnException: throws AggregateException if any handlers failed
   *                   - Parallel strategies: may throw exceptions from handlers
   *
   * @see PublishStrategy.DEFAULT
   * @see PublishStrategy.CONTINUE_ON_EXCEPTION
   * @see PublishStrategy.PARALLEL_WHEN_ALL
   */
  suspend fun <T : Notification> publish(notification: T, publishStrategy: PublishStrategy = PublishStrategy.DEFAULT)

  companion object {
    /**
     * Creates a new Mediator instance with the provided dependency provider.
     *
     * The dependency provider is used to resolve handlers for requests and notifications.
     * It should be configured to provide instances of:
     * - RequestHandler implementations for each request type (queries and commands)
     * - NotificationHandler implementations for each notification type
     * - PipelineBehavior implementations for cross-cutting concerns
     *
     * By default, this method creates a mediator with caching enabled for improved performance.
     * Handler resolution results are cached to avoid repeated lookups for the same request/notification types.
     *
     * @param dependencyProvider The dependency provider that will resolve handler instances
     * @return A configured Mediator instance ready for use
     *
     * @see DependencyProvider
     * @see RequestHandler
     * @see NotificationHandler
     * @see CachedRegistry
     */
    fun build(dependencyProvider: DependencyProvider): Mediator = MediatorImpl(CachedRegistry(RegistryImpl(dependencyProvider)))
  }
}
