package com.trendyol.kediatr

import kotlin.reflect.KClass

/**
 * Dependency provider interface for resolving handler instances and discovering subtypes.
 *
 * This interface abstracts the dependency injection mechanism used by the mediator
 * to resolve handlers for requests and notifications. Implementations
 * should integrate with specific DI frameworks (Spring, Koin, etc.).
 *
 * @see Mediator
 * @see RequestHandler
 * @see NotificationHandler
 */
interface DependencyProvider {
  /**
   * Gets a single instance of the specified class.
   *
   * This method is used to resolve handler instances for requests,
   * which should have exactly one handler per type.
   *
   * @param T The type of instance to resolve
   * @param clazz The class object representing the type to resolve
   * @return A single instance of the specified type
   * @throws Exception if the instance cannot be resolved or multiple instances are found
   */
  fun <T : Any> getSingleInstanceOf(clazz: KClass<T>): T

  /**
   * Gets all subtypes of the specified class.
   *
   * This method is used to discover all implementations of a given interface,
   * particularly for finding notification handlers where multiple handlers
   * can exist for the same notification type.
   *
   * @param T The base type to find subtypes for
   * @param clazz The class object representing the base type
   * @return A collection of all classes that extend or implement the specified type
   */
  fun <T : Any> getSubTypesOf(clazz: KClass<T>): Collection<KClass<T>>
}
