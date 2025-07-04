package com.trendyol.kediatr

/**
 * Internal provider class for creating query handler instances with dependency injection support.
 *
 * This class acts as a factory for query handlers, using the dependency provider
 * to resolve and instantiate handler instances. It's used internally by the Registry
 * to manage the lifecycle and creation of query handlers.
 *
 * @param H The type of query handler that extends QueryHandler
 * @param dependencyProvider The dependency provider used to resolve handler instances
 * @param type The class type of the handler to create
 * @see QueryHandler
 * @see DependencyProvider
 * @see Registry
 */
internal class QueryProvider<H : QueryHandler<*, *>>(
  private val dependencyProvider: DependencyProvider,
  private val type: Class<H>
) {
  /**
   * Creates and returns a new instance of the query handler.
   *
   * @return A new instance of the query handler resolved through the dependency provider
   * @throws Exception if the handler cannot be instantiated or resolved
   */
  fun get(): H = dependencyProvider.getSingleInstanceOf(type)
}
