package com.trendyol.kediatr

import kotlinx.coroutines.*

class ContinueOnExceptionPublishStrategy : PublishStrategy {

    override suspend fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>,
        dispatcher: CoroutineDispatcher,
    ) {
        coroutineScope {
            withContext(dispatcher) {
                val exceptions = mutableListOf<Throwable>()
                notificationHandlers.forEach {
                    try {
                        it.handle(notification)
                    } catch (e: Exception) {
                        exceptions.add(e)
                    }
                }
                if (exceptions.isNotEmpty()) {
                    throw AggregateException(exceptions)
                }
            }
        }
    }
}

class StopOnExceptionPublishStrategy : PublishStrategy {

    override suspend fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>,
        dispatcher: CoroutineDispatcher,
    ) {
        coroutineScope {
            withContext(dispatcher) {
                notificationHandlers.forEach { it.handle(notification) }
            }
        }
    }
}

class ParallelNoWaitPublishStrategy : PublishStrategy {

    override suspend fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>,
        dispatcher: CoroutineDispatcher,
    ) {
        coroutineScope {
            withContext(dispatcher) {
                notificationHandlers.forEach { launch { it.handle(notification) } }
            }
        }
    }
}

class ParallelWhenAllPublishStrategy : PublishStrategy {

    override suspend fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>,
        dispatcher: CoroutineDispatcher,
    ) {
        coroutineScope {
            withContext(dispatcher) {
                val deferredResults = notificationHandlers.map { async { it.handle(notification) } }
                deferredResults.awaitAll()
            }
        }
    }
}
