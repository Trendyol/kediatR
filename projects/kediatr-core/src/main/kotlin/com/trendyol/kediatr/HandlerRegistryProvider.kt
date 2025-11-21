@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

import java.lang.reflect.ParameterizedType

/**
 * Dependency provider implementation that uses a map to resolve dependencies.
 *
 * This is a simple, map-based dependency provider that can be used for testing
 * or scenarios where you want to manually configure handlers without a full
 * dependency injection framework. It stores handler instances in a HashMap
 * and resolves them by their class type.
 *
 * Example usage:
 * ```kotlin
 * val handlers = listOf(
 *     MyRequestHandler(),
 *     MyNotificationHandler(),
 *     LoggingPipelineBehavior()
 * )
 * val mediator = HandlerRegistryProvider.createMediator(handlers)
 * ```
 *
 * @param handlerMap A map that contains the handlers where the key is the handler class
 *                   and the value is the handler instance
 * @see DependencyProvider
 * @see Mediator
 */
class HandlerRegistryProvider(
  private val handlerMap: HashMap<Class<*>, Any>
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
  override fun <T> getSingleInstanceOf(clazz: Class<T>): T = handlerMap[clazz] as T

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
  override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> = handlerMap.keys
    .filter { isCompatibleType(it, clazz) }
    .map { it as Class<T> }

  /**
   * Determines if a handler class is compatible with the specified interface or base class.
   *
   * This method performs sophisticated type checking that handles:
   * - Direct class inheritance and interface implementation
   * - Generic interface implementations (e.g., RequestHandler<MyRequest, String>)
   * - Inheritance chains with generic interfaces
   * - Complex type hierarchies
   *
   * @param T The target type to check compatibility against
   * @param handler The handler class to check
   * @param interfaceOrBaseClass The interface or base class to check compatibility with
   * @return true if the handler is compatible with the specified type, false otherwise
   */
  private fun <T> isCompatibleType(
    handler: Class<*>,
    interfaceOrBaseClass: Class<T>
  ): Boolean = when {
    // Direct assignability check (handles direct inheritance and interface implementation)
    interfaceOrBaseClass.isAssignableFrom(handler) -> true

    // Check if handler directly implements the generic interface
    handler.genericInterfaces
      .filterIsInstance<ParameterizedType>()
      .any { it.rawType == interfaceOrBaseClass } -> true

    // Check inheritance chain for generic interface implementations
    else -> when (val superclass = handler.genericSuperclass) {
      is ParameterizedType -> {
        val inheritedHandler = superclass.rawType as Class<*>
        inheritedHandler.genericInterfaces
          .filterIsInstance<ParameterizedType>()
          .any { it.rawType == interfaceOrBaseClass }
      }

      is Class<*> -> {
        interfaceOrBaseClass.isAssignableFrom(superclass)
      }

      else -> {
        false
      }
    }
  }

  companion object {
    /**
     * Creates a mediator instance with the given handlers using a mapping dependency provider.
     *
     * This is a convenient factory method for creating a mediator with a predefined set of handlers.
     * It's particularly useful for testing scenarios or simple applications that don't require
     * a full dependency injection framework.
     *
     * The handlers list can contain:
     * - RequestHandler implementations for queries and commands
     * - NotificationHandler implementations
     * - PipelineBehavior implementations
     *
     * Example:
     * ```kotlin
     * val handlers = listOf(
     *     GetUserRequestHandler(),
     *     CreateUserRequestHandler(),
     *     UserCreatedNotificationHandler(),
     *     LoggingPipelineBehavior()
     * )
     * val mediator = HandlerRegistryProvider.createMediator(handlers)
     *
     * // Use the mediator
     * val user = mediator.send(GetUserQuery(123))
     * mediator.send(CreateUserCommand("John Doe"))
     * mediator.publish(UserCreatedNotification(user.id))
     * ```
     *
     * @param handlers The handlers to be used by the mediator. Can include any combination of
     *                 RequestHandler, NotificationHandler, and PipelineBehavior instances
     * @return A configured mediator instance ready for use
     * @see RequestHandler
     * @see NotificationHandler
     * @see PipelineBehavior
     */
    fun createMediator(handlers: List<Any> = emptyList()): Mediator {
      val provider = HandlerRegistryProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>)
      val mediator = Mediator.build(provider)
      return mediator
    }
  }
}
