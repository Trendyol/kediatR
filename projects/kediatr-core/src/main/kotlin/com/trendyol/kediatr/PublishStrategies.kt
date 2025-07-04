package com.trendyol.kediatr

import kotlinx.coroutines.*

@Suppress("unused")
interface PublishStrategy {
  /**
   * Publishes a notification to all registered handlers.
   *
   * @param T The type of notification to publish
   * @param notification The notification instance to publish
   * @param notificationHandlers Collection of handlers that will process the notification
   * @param dispatcher The coroutine dispatcher to use for execution (defaults to Dispatchers.IO)
   */
  suspend fun <T : Notification> publish(
    notification: T,
    notificationHandlers: Collection<NotificationHandler<T>>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
  )

  companion object {
    /**
     * Default publish strategy that stops on the first exception.
     */
    val DEFAULT: PublishStrategy = StopOnExceptionPublishStrategy()

    /**
     * Publish strategy that continues processing handlers even if exceptions occur,
     * then throws an aggregate exception containing all failures.
     */
    val CONTINUE_ON_EXCEPTION: PublishStrategy = ContinueOnExceptionPublishStrategy()

    /**
     * Publish strategy that executes handlers in parallel without waiting for completion.
     */
    val PARALLEL_NO_WAIT: PublishStrategy = ParallelNoWaitPublishStrategy()

    /**
     * Publish strategy that executes handlers in parallel and waits for all to complete.
     */
    val PARALLEL_WHEN_ALL: PublishStrategy = ParallelWhenAllPublishStrategy()
  }

  /**
   * Publish strategy that continues executing all handlers even when exceptions occur.
   * Collects all exceptions and throws an [AggregateException] if any handlers failed.
   * Handlers are executed sequentially in the order they are provided.
   */
  class ContinueOnExceptionPublishStrategy : PublishStrategy {
    /**
     * Publishes the notification to all handlers, continuing execution even if some handlers throw exceptions.
     * All exceptions are collected and thrown as an [AggregateException] at the end.
     *
     * @param T The type of notification to publish
     * @param notification The notification instance to publish
     * @param notificationHandlers Collection of handlers that will process the notification
     * @param dispatcher The coroutine dispatcher to use for execution
     * @throws AggregateException if one or more handlers threw exceptions
     */
    override suspend fun <T : Notification> publish(
      notification: T,
      notificationHandlers: Collection<NotificationHandler<T>>,
      dispatcher: CoroutineDispatcher
    ): Unit = withContext(dispatcher) {
      notificationHandlers
        .map { handler -> runCatching { handler.handle(notification) } }
        .mapNotNull { result -> result.exceptionOrNull() }
        .takeIf { exceptions -> exceptions.isNotEmpty() }
        ?.let { exceptions -> throw AggregateException(exceptions) }
    }
  }

  /**
   * Publish strategy that stops execution on the first exception encountered.
   * Handlers are executed sequentially in the order they are provided.
   */
  class StopOnExceptionPublishStrategy : PublishStrategy {
    /**
     * Publishes the notification to handlers sequentially, stopping on the first exception.
     *
     * @param T The type of notification to publish
     * @param notification The notification instance to publish
     * @param notificationHandlers Collection of handlers that will process the notification
     * @param dispatcher The coroutine dispatcher to use for execution
     * @throws Exception if any handler throws an exception
     */
    override suspend fun <T : Notification> publish(
      notification: T,
      notificationHandlers: Collection<NotificationHandler<T>>,
      dispatcher: CoroutineDispatcher
    ) = withContext(dispatcher) { notificationHandlers.forEach { it.handle(notification) } }
  }

  /**
   * Publish strategy that executes all handlers in parallel without waiting for completion.
   * This is a "fire-and-forget" approach where the publish method returns immediately
   * after launching all handler coroutines.
   */
  class ParallelNoWaitPublishStrategy : PublishStrategy {
    /**
     * Publishes the notification to all handlers in parallel without waiting for completion.
     * Each handler is launched in its own coroutine and the method returns immediately.
     *
     * @param T The type of notification to publish
     * @param notification The notification instance to publish
     * @param notificationHandlers Collection of handlers that will process the notification
     * @param dispatcher The coroutine dispatcher to use for execution
     */
    override suspend fun <T : Notification> publish(
      notification: T,
      notificationHandlers: Collection<NotificationHandler<T>>,
      dispatcher: CoroutineDispatcher
    ) = withContext(dispatcher) { notificationHandlers.forEach { launch { it.handle(notification) } } }
  }

  /**
   * Publish strategy that executes all handlers in parallel and waits for all to complete.
   * If any handler throws an exception, it will be propagated after all handlers have completed.
   */
  class ParallelWhenAllPublishStrategy : PublishStrategy {
    /**
     * Publishes the notification to all handlers in parallel and waits for all to complete.
     * Uses async/await pattern to execute handlers concurrently.
     *
     * @param T The type of notification to publish
     * @param notification The notification instance to publish
     * @param notificationHandlers Collection of handlers that will process the notification
     * @param dispatcher The coroutine dispatcher to use for execution
     * @throws Exception if any handler throws an exception
     */
    override suspend fun <T : Notification> publish(
      notification: T,
      notificationHandlers: Collection<NotificationHandler<T>>,
      dispatcher: CoroutineDispatcher
    ): Unit = withContext(dispatcher) { notificationHandlers.map { async { it.handle(notification) } }.awaitAll() }
  }
}
