package com.trendyol.kediatr

/**
 * Internal provider class for creating command handler instances with dependency injection support.
 *
 * This class acts as a factory for command handlers, using the dependency provider
 * to resolve and instantiate handler instances. It's used internally by the Registry
 * to manage the lifecycle and creation of command handlers.
 *
 * @param H The type of command handler that extends CommandHandler
 * @param dependencyProvider The dependency provider used to resolve handler instances
 * @param type The class type of the handler to create
 * @see CommandHandler
 * @see DependencyProvider
 * @see Registry
 */
internal class CommandProvider<H : CommandHandler<*, *>>(
  private val dependencyProvider: DependencyProvider,
  private val type: Class<H>
) {
  /**
   * Creates and returns a new instance of the command handler.
   *
   * @return A new instance of the command handler resolved through the dependency provider
   * @throws Exception if the handler cannot be instantiated or resolved
   */
  fun get(): H = dependencyProvider.getSingleInstanceOf(type)
}
