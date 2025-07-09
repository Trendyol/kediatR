@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

/**
 * Default implementation of the Registry interface.
 *
 * This class provides the concrete implementation for resolving handlers and pipeline behaviors.
 * It uses a Container to manage the registration and storage of handlers, and provides
 * sophisticated lookup mechanisms that support inheritance hierarchies.
 *
 * The implementation supports:
 * - Direct handler lookup by exact type match
 * - Fallback lookup through inheritance chain for base classes
 * - Multiple notification handlers for the same notification type
 * - Polymorphic handler resolution
 *
 * @param dependencyProvider The dependency provider used to discover and resolve handlers
 * @see Registry
 * @see Container
 * @see DependencyProvider
 */
internal class RegistryImpl(
  dependencyProvider: DependencyProvider
) : Registry {
  /**
   * Internal container that manages the registration and storage of all handlers and behaviors.
   */
  private val registry = Container(dependencyProvider)

  /**
   * Resolves the request handler for the specified request type.
   *
   * This method performs a two-stage lookup:
   * 1. Direct lookup by the exact request class
   * 2. Fallback lookup through the inheritance chain to find handlers for base request types
   *
   * This enables polymorphic request handling where a handler for a base request type
   * can process derived request types.
   *
   * @param TRequest The type of request that extends Request<TResult>
   * @param TResult The type of result that the request handler will return
   * @param classOfRequest The class object representing the request type
   * @return The request handler instance for the specified request type
   * @throws HandlerNotFoundException if no handler is found for the request type or its base types
   */
  override fun <TRequest : Request<TResult>, TResult> resolveHandler(
    classOfRequest: Class<TRequest>
  ): RequestHandler<TRequest, TResult> {
    val handler = registry.requestHandlerMap[classOfRequest]?.get()
      ?: registry.requestHandlerMap[baseClassOrItself(classOfRequest, Request::class.java)]?.get()
      ?: throw HandlerNotFoundException(
        requestType = classOfRequest,
        availableHandlers = registry.requestHandlerMap.keys.toList()
      )
    return handler as RequestHandler<TRequest, TResult>
  }

  /**
   * Resolves all notification handlers for the specified notification type.
   *
   * This method finds all handlers that can process the given notification type,
   * including handlers registered for base notification types. It supports
   * polymorphic notification handling where handlers for base types can process
   * derived notification types.
   *
   * @param TNotification The type of notification that extends Notification
   * @param classOfNotification The class object representing the notification type
   * @return A collection of all notification handlers that can process the specified notification type
   */
  override fun <TNotification : Notification> resolveNotificationHandlers(
    classOfNotification: Class<TNotification>
  ): Collection<NotificationHandler<TNotification>> = registry.notificationMap
    .filter { (k, _) -> k.isAssignableFrom(classOfNotification) }
    .flatMap { (_, v) -> v.map { it.get() as NotificationHandler<TNotification> } }

  /**
   * Gets all registered pipeline behaviors.
   *
   * This method retrieves all pipeline behaviors that have been registered with the container.
   * The behaviors will be sorted by their precedence values when used by the mediator.
   *
   * @return A collection of all registered pipeline behaviors
   */
  override fun getPipelineBehaviors(): Collection<PipelineBehavior> = registry.pipelineSet.map { it.get() }

  /**
   * Walk the inheritance chain of [clazz], collect all classes (including itself)
   * that implement/extend [clazzWanted], and return the farthest‐up one.
   *
   * This method enables polymorphic handler resolution by finding the most specific
   * base class that matches the desired interface. It's used to support scenarios
   * where a handler is registered for a base request type but needs to handle
   * derived types.
   *
   * Example:
   * ```kotlin
   * abstract class BaseRequest : Request<Unit>
   * class SpecificRequest : BaseRequest()
   *
   * // Handler registered for BaseRequest
   * class BaseRequestHandler : RequestHandler<BaseRequest, Unit>
   *
   * // This method will find BaseRequest when looking up SpecificRequest
   * ```
   *
   * - Uses a purely functional style: generateSequence → filter → lastOrNull.
   * - Avoids any explicit nullable vars or while‐loops.
   *
   * @param clazz The starting class to inspect.
   * @param clazzWanted The interface or superclass we're looking for.
   * @return The highest ancestor of [clazz] (or [clazz] itself) for which
   *         clazzWanted.isAssignableFrom(thatAncestor) is true. If none match,
   *         returns [clazz].
   */
  private fun baseClassOrItself(
    clazz: Class<*>,
    clazzWanted: Class<*>
  ): Class<*> =
    generateSequence(clazz) { it.superclass } // ↪ generate the chain: clazz, clazz.superclass, clazz.superclass.superclass, …
      .filter { clazzWanted.isAssignableFrom(it) } // ↪ keep only those that actually "implement/extend" clazzWanted
      .lastOrNull() // ↪ pick the *last* (farthest‐up) match
      ?: clazz // ↪ if none matched, return the original clazz
}
