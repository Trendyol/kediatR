package com.trendyol.kediatr

import kotlin.reflect.KClass

/**
 * Internal provider class for creating request handler instances with dependency injection support.
 *
 * This class acts as a factory for request handlers, using the dependency provider
 * to resolve and instantiate handler instances. It's used internally by the Registry
 * to manage the lifecycle and creation of request handlers.
 *
 * @param H The type of request handler that extends RequestHandler
 * @param dependencyProvider The dependency provider used to resolve handler instances
 * @param type The class type of the handler to create
 * @see DependencyProvider
 * @see Registry
 */
internal class RequestProvider<H : RequestHandler<*, *>>(
  private val dependencyProvider: DependencyProvider,
  private val type: KClass<H>
) {
  /**
   * Creates and returns a new instance of the request handler.
   *
   * @return A new instance of the request handler resolved through the dependency provider
   * @throws Exception if the handler cannot be instantiated or resolved
   */
  fun get(): H = dependencyProvider.getSingleInstanceOf(type)
}
