package com.trendyol.kediatr

/**
 * Internal provider class for creating notification handler instances with dependency injection support.
 *
 * This class acts as a factory for notification handlers, using the dependency provider
 * to resolve and instantiate handler instances. It's used internally by the Registry
 * to manage the lifecycle and creation of notification handlers. Unlike query and command
 * handlers, multiple notification handlers can exist for the same notification type.
 *
 * @param H The type of notification handler that extends NotificationHandler
 * @param dependencyProvider The dependency provider used to resolve handler instances
 * @param type The class type of the handler to create
 * @see NotificationHandler
 * @see DependencyProvider
 * @see Registry
 */
internal class NotificationProvider<H : NotificationHandler<*>>(
  private val dependencyProvider: DependencyProvider,
  private val type: Class<H>
) {
  /**
   * Creates and returns a new instance of the notification handler.
   *
   * @return A new instance of the notification handler resolved through the dependency provider
   * @throws Exception if the handler cannot be instantiated or resolved
   */
  fun get(): H = dependencyProvider.getSingleInstanceOf(type)
}
