package com.trendyol.kediatr

import kotlin.reflect.KClass

/**
 * Dependency provider implementation that uses a map to resolve dependencies.
 *
 * This is a simple, map-based dependency provider that can be used for testing
 * or scenarios where you want to manually configure handlers without a full
 * dependency injection framework. It stores handler instances in a HashMap
 * and resolves them by their class type.
 *
 * @param handlerMap A map that contains the handlers where the key is the handler class
 *                   and the value is the handler instance
 * @see DependencyProvider
 * @see Mediator
 */
@Suppress("UNCHECKED_CAST")
class HandlerRegistryProvider(
  private val handlerMap: Map<KClass<*>, Any>
) : DependencyProvider {
  /**
   * Gets a single instance of the specified class from the handler map.
   *
   * @param T The type of instance to resolve
   * @param clazz The class object representing the type to resolve
   * @return The handler instance from the map
   * @throws ClassCastException if the stored instance cannot be cast to the requested type
   * @throws NullPointerException if no handler is found for the specified class
   */
  override fun <T : Any> getSingleInstanceOf(clazz: KClass<T>): T = handlerMap[clazz] as T

  /**
   * Gets all subtypes of the specified class from the handler map.
   *
   * This method searches through all registered handlers and returns those that
   * are compatible with the specified base class or interface. It supports
   * complex type hierarchies including generic interfaces and inheritance chains.
   *
   * @param T The base type to find subtypes for
   * @param clazz The class object representing the base type
   * @return A collection of all classes that extend or implement the specified type
   */
  override fun <T : Any> getSubTypesOf(clazz: KClass<T>): Collection<KClass<T>> = handlerMap.keys
    .filter { ReflectionUtils.isAssignableFrom(clazz, it) }
    .map { it as KClass<T> }

  companion object {
    /**
     * Creates a mediator instance with the given handlers using a mapping dependency provider.
     *
     * This is a convenient factory method for creating a mediator with a predefined set of handlers.
     * It's particularly useful for testing scenarios or simple applications that don't require
     * a full dependency injection framework.
     *
     * @param handlers The handlers to be used by the mediator. Can include any combination of
     *                 RequestHandler, NotificationHandler, and PipelineBehavior instances
     * @return A configured mediator instance ready for use
     * @see RequestHandler
     * @see NotificationHandler
     * @see PipelineBehavior
     */
    fun createMediator(handlers: List<Any> = emptyList()): Mediator {
      val provider = HandlerRegistryProvider(handlers.associateBy { it::class })
      val mediator = Mediator.build(provider)
      return mediator
    }
  }
}
