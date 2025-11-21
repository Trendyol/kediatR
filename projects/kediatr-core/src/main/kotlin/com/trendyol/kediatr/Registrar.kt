package com.trendyol.kediatr

import java.lang.reflect.*

/**
 * Abstract base class that provides handler registration functionality.
 *
 * This class contains the core logic for discovering and registering handlers from a dependency provider.
 * It uses reflection to analyze handler implementations and extract the types they handle, then
 * registers them appropriately. The registration process supports complex type hierarchies including
 * generic interfaces, inheritance chains, and parameterized types.
 *
 * The Registrar is used by the Container class to automatically discover and register:
 * - RequestHandler implementations for queries and commands
 * - NotificationHandler implementations
 * - PipelineBehavior implementations
 *
 * @see Container
 * @see DependencyProvider
 */
@Suppress("UNCHECKED_CAST")
internal abstract class Registrar {
  /**
   * Registers handlers that handle a specific parameter type (used for requests and notifications).
   *
   * This method discovers all implementations of the specified handler type from the dependency provider
   * and registers them with their corresponding parameter types. It's used for handlers that process
   * specific message types (requests, notifications).
   *
   * @param THandler The type of handler to register (e.g., RequestHandler, NotificationHandler)
   * @param TParameter The type of parameter the handler processes (e.g., Request, Notification)
   * @param dependencyProvider The dependency provider to discover handlers from
   * @param registrar The callback function to register each handler with its parameter type
   */
  protected inline fun <reified THandler : Any, TParameter> registerFor(
    dependencyProvider: DependencyProvider,
    registrar: (key: Class<TParameter>, value: Class<THandler>) -> Unit
  ) = dependencyProvider
    .getSubTypesOf(THandler::class.java)
    .filter { Modifier.isAbstract(it.modifiers).not() }
    .forEach {
      registerFor<THandler, TParameter>(it) { key, value ->
        registrar(key as Class<TParameter>, value as Class<THandler>)
      }
    }

  /**
   * Registers a specific handler class with its parameter type.
   *
   * This method analyzes the handler class using reflection to determine what parameter types
   * it can handle. It examines generic interfaces and inheritance chains to extract the
   * appropriate parameter types and register the handler for each one.
   *
   * @param THandler The type of handler being registered
   * @param TParameter The type of parameter the handler processes
   * @param handler The specific handler class to register
   * @param registrar The callback function to register the handler with its parameter type
   */
  protected inline fun <reified THandler : Any, TParameter> registerFor(
    handler: Class<*>,
    registrar: (key: Class<*>, value: Class<*>) -> Unit
  ) {
    val interfaceOrBaseClass = THandler::class.java
    if (!interfaceOrBaseClass.isAssignableFrom(handler)) return

    // Register handlers based on directly implemented generic interfaces
    handler.genericInterfaces
      .filterIsInstance<ParameterizedType>()
      .map { extractParameter(it) }
      .forEach { registrar(it, handler) }

    // Handle inheritance chains where handlers inherit from base classes
    when (handler.genericSuperclass) {
      is ParameterizedType -> {
        val inheritedHandler = (handler.genericSuperclass as ParameterizedType).rawType as Class<*>
        inheritedHandler.genericInterfaces
          .filterIsInstance<ParameterizedType>()
          .map { extractParameter(handler.genericSuperclass as ParameterizedType) }
          .forEach { registrar(it, handler) }
      }

      is Class<*> -> {
        val inheritedHandler = handler.genericSuperclass as Class<*>
        if (interfaceOrBaseClass.isAssignableFrom(inheritedHandler)) {
          inheritedHandler.genericInterfaces
            .filterIsInstance<ParameterizedType>()
            .map { extractParameter(it) }
            .forEach { registrar(it, handler) }
        }
      }
    }
  }

  /**
   * Registers handlers that don't require parameter type mapping (used for pipeline behaviors).
   *
   * This method discovers all implementations of the specified type from the dependency provider
   * and registers them directly without parameter type extraction. It's used for pipeline behaviors
   * which apply to all requests regardless of their specific type.
   *
   * @param T The type of handler to register (e.g., PipelineBehavior)
   * @param dependencyProvider The dependency provider to discover handlers from
   * @param registrar The callback function to register each handler
   */
  protected inline fun <reified T> registerFor(
    dependencyProvider: DependencyProvider,
    registrar: (value: Class<T>) -> Unit
  ) = dependencyProvider.getSubTypesOf(T::class.java).forEach { handler ->
    registerFor<T>(handler) { value -> registrar(value as Class<T>) }
  }

  /**
   * Registers a specific handler class without parameter type mapping.
   *
   * This method simply checks if the handler class is assignable to the target type
   * and registers it if compatible. Used for simple registration scenarios.
   *
   * @param T The type of handler being registered
   * @param handler The specific handler class to register
   * @param registrar The callback function to register the handler
   */
  protected inline fun <reified T> registerFor(
    handler: Class<*>,
    registrar: (value: Class<*>) -> Unit
  ) {
    val interfaceOrBaseClass = T::class.java
    if (!interfaceOrBaseClass.isAssignableFrom(handler)) return
    registrar(handler)
  }

  /**
   * Extracts the parameter type from a generic interface using reflection.
   *
   * This method analyzes parameterized types to extract the actual type arguments,
   * handling various scenarios including:
   * - Direct parameterized types (e.g., RequestHandler<MyRequest, String>)
   * - Type variables with bounds
   * - Nested generic types
   * - Inheritance chains with generic interfaces
   *
   * @param genericInterface The parameterized type to extract the parameter from
   * @return The extracted parameter class
   */
  protected fun extractParameter(genericInterface: ParameterizedType): Class<*> =
    when (val typeArgument = genericInterface.actualTypeArguments[0]) {
      is ParameterizedType -> {
        typeArgument.rawType as Class<*>
      }

      is TypeVariable<*> -> {
        val rawType = (genericInterface.rawType as Class<*>)
        when {
          rawType.genericInterfaces.any() -> extractParameter(rawType.genericInterfaces[0] as ParameterizedType)
          else -> rawType
        }
      }

      else -> {
        typeArgument as Class<*>
      }
    }
}
