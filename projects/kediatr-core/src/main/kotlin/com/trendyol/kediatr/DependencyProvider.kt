package com.trendyol.kediatr

/**
 * Dependency provider interface.
 */
interface DependencyProvider {
  /**
   * Gets a single instance of the specified class.
   */
  fun <T> getSingleInstanceOf(clazz: Class<T>): T

  /**
   * Gets all subtypes of the specified class.
   */
  fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>>
}
