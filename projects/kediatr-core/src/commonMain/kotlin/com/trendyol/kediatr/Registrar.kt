package com.trendyol.kediatr

import kotlin.reflect.KClass

/**
 * Abstract base class that provides handler registration functionality.
 *
 * This class contains the core logic for discovering and registering handlers from a dependency provider.
 * It uses ReflectionUtils to analyze handler implementations and extract the types they handle.
 *
 * @see Container
 * @see DependencyProvider
 */
@Suppress("UNCHECKED_CAST")
internal abstract class Registrar {
  /**
   * Registers handlers that handle a specific parameter type (used for requests and notifications).
   */
  protected inline fun <reified THandler : Any, TParameter : Any> registerFor(
    dependencyProvider: DependencyProvider,
    noinline registrar: (key: KClass<TParameter>, value: KClass<THandler>) -> Unit
  ) {
    val handlerInterface = THandler::class
    dependencyProvider.getSubTypesOf(handlerInterface)
      .filter { !ReflectionUtils.isAbstract(it) }
      .forEach { handler ->
         val paramType = ReflectionUtils.getHandlerParameterType(handler, handlerInterface)
         if (paramType != null) {
             registrar(paramType as KClass<TParameter>, handler as KClass<THandler>)
         }
      }
  }

  /**
   * Registers handlers that don't require parameter type mapping (used for pipeline behaviors).
   */
  protected inline fun <reified T : Any> registerFor(
    dependencyProvider: DependencyProvider,
    noinline registrar: (value: KClass<T>) -> Unit
  ) {
      dependencyProvider.getSubTypesOf(T::class)
        .forEach { handler ->
             if (!ReflectionUtils.isAbstract(handler)) {
                 registrar(handler)
             }
        }
  }
}
