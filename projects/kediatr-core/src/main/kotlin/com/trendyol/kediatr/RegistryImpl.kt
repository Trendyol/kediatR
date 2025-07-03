@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

/**
 * Default implementation of the Registry interface.
 *
 * This class provides the concrete implementation for resolving handlers and pipeline behaviors.
 * It uses a Container to manage the registration and storage of handlers, and provides
 * sophisticated lookup mechanisms that support inheritance hierarchies.
 *
 * The implementation supports:
 * - Direct handler lookup by exact type match
 * - Fallback lookup through inheritance chain for base classes
 * - Multiple notification handlers for the same notification type
 * - Polymorphic handler resolution
 *
 * @param dependencyProvider The dependency provider used to discover and resolve handlers
 * @see Registry
 * @see Container
 * @see DependencyProvider
 */
class RegistryImpl(
  dependencyProvider: DependencyProvider
) : Registry {
  /**
   * Internal container that manages the registration and storage of all handlers and behaviors.
   */
  private val registry = Container(dependencyProvider)

  /**
   * Resolves the command handler for the specified command type.
   *
   * This method performs a two-stage lookup:
   * 1. Direct lookup by the exact command class
   * 2. Fallback lookup through the inheritance chain to find handlers for base command types
   *
   * This enables polymorphic command handling where a handler for a base command type
   * can process derived command types.
   *
   * @param TCommand The type of command that extends Command<TResult>
   * @param TResult The type of result that the command handler will return
   * @param classOfCommand The class object representing the command type
   * @return The command handler instance for the specified command type
   * @throws HandlerNotFoundException if no handler is found for the command type or its base types
   */
  override fun <TCommand : Command<TResult>, TResult> resolveCommandHandler(
    classOfCommand: Class<TCommand>
  ): CommandHandler<TCommand, TResult> {
    val handler = registry.commandMap[classOfCommand]?.get()
      ?: registry.commandMap[baseClassOrItself(classOfCommand, Command::class.java)]?.get()
      ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
    return handler as CommandHandler<TCommand, TResult>
  }

  /**
   * Resolves all notification handlers for the specified notification type.
   *
   * This method finds all handlers that can process the given notification type,
   * including handlers registered for base notification types. It supports
   * polymorphic notification handling where handlers for base types can process
   * derived notification types.
   *
   * @param TNotification The type of notification that extends Notification
   * @param classOfNotification The class object representing the notification type
   * @return A collection of all notification handlers that can process the specified notification type
   */
  override fun <TNotification : Notification> resolveNotificationHandlers(
    classOfNotification: Class<TNotification>
  ): Collection<NotificationHandler<TNotification>> = registry.notificationMap
    .filter { (k, _) -> k.isAssignableFrom(classOfNotification) }
    .flatMap { (_, v) -> v.map { it.get() as NotificationHandler<TNotification> } }

  /**
   * Resolves the query handler for the specified query type.
   *
   * This method performs a two-stage lookup:
   * 1. Direct lookup by the exact query class
   * 2. Fallback lookup through the inheritance chain to find handlers for base query types
   *
   * This enables polymorphic query handling where a handler for a base query type
   * can process derived query types.
   *
   * @param TQuery The type of query that extends Query<TResult>
   * @param TResult The type of result that the query handler will return
   * @param classOfQuery The class object representing the query type
   * @return The query handler instance for the specified query type
   * @throws HandlerNotFoundException if no handler is found for the query type or its base types
   */
  override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery, TResult> {
    val handler = registry.queryMap[classOfQuery]?.get()
      ?: registry.queryMap[baseClassOrItself(classOfQuery, Query::class.java)]?.get()
      ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
    return handler as QueryHandler<TQuery, TResult>
  }

  /**
   * Gets all registered pipeline behaviors.
   *
   * This method retrieves all pipeline behaviors that have been registered with the container.
   * The behaviors will be sorted by their precedence values when used by the mediator.
   *
   * @return A collection of all registered pipeline behaviors
   */
  override fun getPipelineBehaviors(): Collection<PipelineBehavior> = registry.pipelineSet.map { it.get() }

  /**
   * Walk the inheritance chain of [clazz], collect all classes (including itself)
   * that implement/extend [clazzWanted], and return the farthest‐up one.
   *
   * This method enables polymorphic handler resolution by finding the most specific
   * base class that matches the desired interface. It's used to support scenarios
   * where a handler is registered for a base command/query type but needs to handle
   * derived types.
   *
   * Example:
   * ```kotlin
   * abstract class BaseCommand : Command<Unit>
   * class SpecificCommand : BaseCommand()
   *
   * // Handler registered for BaseCommand
   * class BaseCommandHandler : CommandHandler<BaseCommand, Unit>
   *
   * // This method will find BaseCommand when looking up SpecificCommand
   * ```
   *
   * - Uses a purely functional style: generateSequence → filter → lastOrNull.
   * - Avoids any explicit nullable vars or while‐loops.
   *
   * @param clazz The starting class to inspect.
   * @param clazzWanted The interface or superclass we're looking for.
   * @return The highest ancestor of [clazz] (or [clazz] itself) for which
   *         clazzWanted.isAssignableFrom(thatAncestor) is true. If none match,
   *         returns [clazz].
   */
  private fun baseClassOrItself(
    clazz: Class<*>,
    clazzWanted: Class<*>
  ): Class<*> =
    generateSequence(clazz) { it.superclass } // ↪ generate the chain: clazz, clazz.superclass, clazz.superclass.superclass, …
      .filter { clazzWanted.isAssignableFrom(it) } // ↪ keep only those that actually "implement/extend" clazzWanted
      .lastOrNull() // ↪ pick the *last* (farthest‐up) match
      ?: clazz // ↪ if none matched, return the original clazz
}
