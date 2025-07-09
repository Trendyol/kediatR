package com.trendyol.kediatr

/**
 * Internal container class that manages the registration and storage of handlers and pipeline behaviors.
 *
 * This class extends Registrar and acts as a central registry that discovers and organizes
 * all handlers and behaviors from the dependency provider. It maintains separate collections
 * for different types of handlers:
 * - Request handlers (one-to-one mapping for both queries and commands)
 * - Notification handlers (one-to-many mapping)
 * - Pipeline behaviors (set of unique behaviors)
 *
 * The container automatically discovers handlers during initialization by scanning the
 * dependency provider for implementations of handler interfaces.
 *
 * @param dependencyProvider The dependency provider used to discover and resolve handlers
 * @see Registrar
 * @see RequestProvider
 * @see NotificationProvider
 * @see PipelineProvider
 */
@Suppress("UNCHECKED_CAST")
internal class Container(
  dependencyProvider: DependencyProvider
) : Registrar() {
  /**
   * Map storing request handlers where the key is the request class and the value is the request provider.
   * Each request type should have exactly one handler.
   */
  val requestHandlerMap = HashMap<Class<*>, RequestProvider<RequestHandler<Request<*>, *>>>()

  /**
   * Map storing notification handlers where the key is the notification class and the value is a list of providers.
   * Multiple handlers can be registered for the same notification type.
   */
  val notificationMap = HashMap<Class<*>, MutableList<NotificationProvider<NotificationHandler<*>>>>()

  /**
   * Set storing all pipeline behavior providers. Pipeline behaviors are applied to all requests.
   */
  val pipelineSet = HashSet<PipelineProvider<*>>()

  init {
    registerFor<RequestHandler<Request<*>, *>, Request<*>>(dependencyProvider) { key, value ->
      if (requestHandlerMap.containsKey(key)) {
        error(
          "Multiple handlers registered for request type: ${key.name}" +
            "\nExisting handler: ${requestHandlerMap[key]!!.get().javaClass.name}" +
            "\nDuplicate handler: ${value.name}"
        )
      }
      requestHandlerMap[key] = RequestProvider(dependencyProvider, value)
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
