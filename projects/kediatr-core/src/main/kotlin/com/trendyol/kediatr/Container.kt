package com.trendyol.kediatr

/**
 * Internal container class that manages the registration and storage of handlers and pipeline behaviors.
 *
 * This class extends Registrar and acts as a central registry that discovers and organizes
 * all handlers and behaviors from the dependency provider. It maintains separate collections
 * for different types of handlers:
 * - Query handlers (one-to-one mapping)
 * - Command handlers (one-to-one mapping)
 * - Notification handlers (one-to-many mapping)
 * - Pipeline behaviors (set of unique behaviors)
 *
 * The container automatically discovers handlers during initialization by scanning the
 * dependency provider for implementations of handler interfaces.
 *
 * @param dependencyProvider The dependency provider used to discover and resolve handlers
 * @see Registrar
 * @see QueryProvider
 * @see CommandProvider
 * @see NotificationProvider
 * @see PipelineProvider
 */
@Suppress("UNCHECKED_CAST")
internal class Container(
  dependencyProvider: DependencyProvider
) : Registrar() {
  /**
   * Map storing query handlers where the key is the query class and the value is the query provider.
   * Each query type should have exactly one handler.
   */
  val queryMap = HashMap<Class<*>, QueryProvider<QueryHandler<*, *>>>()

  /**
   * Map storing notification handlers where the key is the notification class and the value is a list of providers.
   * Multiple handlers can be registered for the same notification type.
   */
  val notificationMap = HashMap<Class<*>, MutableList<NotificationProvider<NotificationHandler<*>>>>()

  /**
   * Set storing all pipeline behavior providers. Pipeline behaviors are applied to all requests.
   */
  val pipelineSet = HashSet<PipelineProvider<*>>()

  /**
   * Map storing command handlers where the key is the command class and the value is the command provider.
   * Each command type should have exactly one handler.
   */
  val commandMap = HashMap<Class<*>, CommandProvider<*>>()

  init {
    // Register query handlers - one handler per query type
    registerFor<QueryHandler<Query<*>, *>, Query<*>>(dependencyProvider) { key, value ->
      queryMap[key] = QueryProvider(dependencyProvider, value as Class<QueryHandler<*, *>>)
    }

    // Register command handlers - one handler per command type
    registerFor<CommandHandler<Command<*>, *>, Command<*>>(dependencyProvider) { key, value ->
      commandMap[key] =
        CommandProvider(
          dependencyProvider,
          value as Class<CommandHandler<*, *>>
        )
    }

    // Register notification handlers - multiple handlers per notification type allowed
    registerFor<NotificationHandler<Notification>, Notification>(dependencyProvider) { key, value ->
      notificationMap
        .getOrPut(key) { mutableListOf() }
        .add(NotificationProvider(dependencyProvider, value as Class<NotificationHandler<*>>))
    }

    // Register pipeline behaviors - applied to all requests
    registerFor<PipelineBehavior>(dependencyProvider) { handler ->
      pipelineSet.add(PipelineProvider(dependencyProvider, handler))
    }
  }
}
