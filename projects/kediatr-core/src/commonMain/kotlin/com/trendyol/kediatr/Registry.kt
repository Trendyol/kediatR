package com.trendyol.kediatr

import kotlin.reflect.KClass

/**
 * Registry interface for resolving handlers and pipeline behaviors.
 *
 * The Registry acts as a central component that uses the dependency provider
 * to resolve and cache handler instances. It provides methods to find the
 * appropriate handlers for requests and notifications, as well as
 * any pipeline behaviors that should be applied.
 *
 * @see DependencyProvider
 * @see RequestHandler
 * @see NotificationHandler
 * @see PipelineBehavior
 */
internal interface Registry {
  /**
   * Resolves the request handler for the specified request type.
   *
   * Each request type (query or command) should have exactly one handler. This method will throw
   * an exception if no handler or multiple handlers are found.
   *
   * @param TRequest The type of request that extends Request<TResult>
   * @param TResult The type of result that the request handler will return
   * @param classOfRequest The class object representing the request type
   * @return The request handler instance for the specified request type
   * @throws HandlerNotFoundException if no handler is found for the request type
   * @throws IllegalStateException if multiple handlers are found for the request type
   */
  fun <TRequest : Request<TResult>, TResult> resolveHandler(classOfRequest: KClass<TRequest>): RequestHandler<TRequest, TResult>

  /**
   * Resolves all notification handlers for the specified notification type.
   *
   * Unlike requests, notifications can have zero, one, or multiple handlers.
   * This method returns all handlers that can process the given notification type.
   *
   * @param TNotification The type of notification that extends Notification
   * @param classOfNotification The class object representing the notification type
   * @return A collection of all notification handlers for the specified notification type
   */
  fun <TNotification : Notification> resolveNotificationHandlers(
    classOfNotification: KClass<TNotification>
  ): Collection<NotificationHandler<TNotification>>

  /**
   * Gets all registered pipeline behaviors.
   *
   * Pipeline behaviors are cross-cutting concerns that can be applied to all
   * requests to provide functionality like logging, validation, caching,
   * transaction management, etc.
   *
   * @return A collection of all registered pipeline behaviors
   * @see PipelineBehavior
   */
  fun getPipelineBehaviors(): Collection<PipelineBehavior>
}
