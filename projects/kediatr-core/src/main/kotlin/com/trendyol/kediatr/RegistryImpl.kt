@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

class RegistryImpl(
  dependencyProvider: DependencyProvider
) : Registry {
  private val registry = Container(dependencyProvider)

  override fun <TCommand : Command> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand> {
    val handler = registry.commandMap[baseClassOrItself(classOfCommand, Command::class.java)]?.get()
      ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
    return handler as CommandHandler<TCommand>
  }

  override fun <TCommand : CommandWithResult<TResult>, TResult> resolveCommandWithResultHandler(
    classOfCommand: Class<TCommand>
  ): CommandWithResultHandler<TCommand, TResult> {
    val handler = registry.commandWithResultMap[baseClassOrItself(classOfCommand, CommandWithResult::class.java)]?.get()
      ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
    return handler as CommandWithResultHandler<TCommand, TResult>
  }

  override fun <TNotification : Notification> resolveNotificationHandlers(
    classOfNotification: Class<TNotification>
  ): Collection<NotificationHandler<TNotification>> = registry.notificationMap
    .filter { (k, _) -> k.isAssignableFrom(classOfNotification) }
    .flatMap { (_, v) -> v.map { it.get() as NotificationHandler<TNotification> } }

  override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery, TResult> {
    val handler = registry.queryMap[baseClassOrItself(classOfQuery, Query::class.java)]?.get()
      ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
    return handler as QueryHandler<TQuery, TResult>
  }

  override fun getPipelineBehaviors(): Collection<PipelineBehavior> = registry.pipelineSet.map { it.get() }

  /**
   * Walk the inheritance chain of [clazz], collect all classes (including itself)
   * that implement/extend [clazzWanted], and return the farthest‐up one.
   *
   * - Uses a purely functional style: generateSequence → filter → lastOrNull.
   * - Avoids any explicit nullable vars or while‐loops.
   *
   * @param clazz The starting class to inspect.
   * @param clazzWanted The interface or superclass we’re looking for.
   * @return The highest ancestor of [clazz] (or [clazz] itself) for which
   *         clazzWanted.isAssignableFrom(thatAncestor) is true. If none match,
   *         returns [clazz].
   */
  private fun baseClassOrItself(
    clazz: Class<*>,
    clazzWanted: Class<*>
  ): Class<*> =
    generateSequence(clazz) { it.superclass } // ↪ generate the chain: clazz, clazz.superclass, clazz.superclass.superclass, …
      .filter { clazzWanted.isAssignableFrom(it) } // ↪ keep only those that actually “implement/extend” clazzWanted
      .lastOrNull() // ↪ pick the *last* (farthest‐up) match
      ?: clazz // ↪ if none matched, return the original clazz
}
