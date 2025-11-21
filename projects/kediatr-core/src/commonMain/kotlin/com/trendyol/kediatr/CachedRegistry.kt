package com.trendyol.kediatr

import kotlin.reflect.KClass

/**
 * A caching wrapper around the Registry interface that improves performance by caching resolved handlers.
 *
 * This implementation provides significant performance improvements by avoiding repeated handler resolution
 * for the same request/notification types. It uses thread-safe concurrent maps to ensure proper behavior
 * in multi-threaded environments.
 *
 * The caching strategy:
 * - Request handlers are cached by their exact class type
 * - Notification handlers are cached as collections by their class type
 * - Pipeline behaviors are cached as a single collection since they apply to all requests
 * - All caches use ConcurrentHashMap for thread safety without synchronization overhead
 *
 * Cache invalidation is not implemented as handlers are typically registered once during application
 * startup and remain constant throughout the application lifecycle.
 *
 * @param delegate The underlying registry implementation that performs the actual handler resolution
 * @see Registry
 * @see RegistryImpl
 */
@Suppress("UNCHECKED_CAST")
internal class CachedRegistry(
  private val delegate: Registry
) : Registry {
  /**
   * Cache for request handlers. Key is the request class, value is the resolved handler.
   * Uses ConcurrentHashMap for thread-safe access without explicit synchronization.
   */
  private val requestHandlerCache = createConcurrentMap<KClass<*>, RequestHandler<*, *>>()

  /**
   * Cache for notification handlers. Key is the notification class, value is the collection of handlers.
   * Uses ConcurrentHashMap for thread-safe access without explicit synchronization.
   */
  private val notificationHandlerCache = createConcurrentMap<KClass<*>, Collection<NotificationHandler<*>>>()

  /**
   * Cache for pipeline behaviors. Since pipeline behaviors apply to all requests,
   * we only need to cache them once as a single collection.
   * Uses lazy initialization with thread-safe delegate.
   */
  private val pipelineBehaviorCache by lazy { delegate.getPipelineBehaviors() }

  /**
   * Resolves the request handler for the specified request type with caching.
   *
   * This method first checks the cache for an existing handler. If found, it returns the cached
   * handler immediately. If not found, it delegates to the underlying registry, caches the result,
   * and returns it.
   *
   * The cache key is the exact request class, which ensures type safety and proper handler resolution.
   * The cache uses computeIfAbsent for atomic cache population in concurrent scenarios.
   *
   * @param TRequest The type of request that extends Request<TResult>
   * @param TResult The type of result that the request handler will return
   * @param classOfRequest The class object representing the request type
   * @return The cached or newly resolved request handler instance
   * @throws HandlerNotFoundException if no handler is found for the request type
   */
  override fun <TRequest : Request<TResult>, TResult> resolveHandler(
    classOfRequest: KClass<TRequest>
  ): RequestHandler<TRequest, TResult> = requestHandlerCache.getOrPut(classOfRequest) {
    delegate.resolveHandler(classOfRequest)
  } as RequestHandler<TRequest, TResult>

  /**
   * Resolves all notification handlers for the specified notification type with caching.
   *
   * This method first checks the cache for existing handlers. If found, it returns the cached
   * collection immediately. If not found, it delegates to the underlying registry, caches the result,
   * and returns it.
   *
   * The cache key is the exact notification class. The cached collection includes all handlers
   * that can process the given notification type, including handlers for base types due to
   * polymorphic resolution in the underlying registry.
   *
   * @param TNotification The type of notification that extends Notification
   * @param classOfNotification The class object representing the notification type
   * @return The cached or newly resolved collection of notification handlers
   */
  override fun <TNotification : Notification> resolveNotificationHandlers(
    classOfNotification: KClass<TNotification>
  ): Collection<NotificationHandler<TNotification>> = notificationHandlerCache.getOrPut(classOfNotification) {
    delegate.resolveNotificationHandlers(classOfNotification)
  } as Collection<NotificationHandler<TNotification>>

  /**
   * Gets all registered pipeline behaviors with caching.
   *
   * Pipeline behaviors are cached using lazy initialization since they apply to all requests
   * and don't vary by request type. The lazy delegate ensures thread-safe initialization
   * and the result is cached for subsequent calls.
   *
   * @return The cached collection of all registered pipeline behaviors
   */
  override fun getPipelineBehaviors(): Collection<PipelineBehavior> = pipelineBehaviorCache

  /**
   * Returns cache statistics for monitoring and debugging purposes.
   *
   * This method provides insight into cache performance and can be useful for
   * monitoring cache hit rates and identifying potential performance issues.
   *
   * @return CacheStatistics containing information about cache sizes and performance
   */
  internal fun getCacheStatistics(): CacheStatistics = CacheStatistics(
    requestHandlerCacheSize = requestHandlerCache.size,
    notificationHandlerCacheSize = notificationHandlerCache.size,
    pipelineBehaviorCacheSize = pipelineBehaviorCache.size
  )
}

/**
 * Data class containing cache statistics for monitoring and debugging.
 *
 * @param requestHandlerCacheSize Number of cached request handlers
 * @param notificationHandlerCacheSize Number of cached notification handler collections
 * @param pipelineBehaviorCacheSize Number of cached pipeline behaviors
 */
internal data class CacheStatistics(
  val requestHandlerCacheSize: Int,
  val notificationHandlerCacheSize: Int,
  val pipelineBehaviorCacheSize: Int
)
