package com.trendyol.kediatr

import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture

class ContinueOnExceptionPublishStrategy : PublishStrategy {

    override fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>
    ) {
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

    override suspend fun <T : Notification> publishAsync(
        notification: T,
        notificationHandlers: Collection<AsyncNotificationHandler<T>>,
        dispatcher: CoroutineDispatcher
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

    override fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>
    ) {
        notificationHandlers.forEach { it.handle(notification) }
    }

    override suspend fun <T : Notification> publishAsync(
        notification: T,
        notificationHandlers: Collection<AsyncNotificationHandler<T>>,
        dispatcher: CoroutineDispatcher
    ) {
        coroutineScope {
            withContext(dispatcher) {
                notificationHandlers.forEach { it.handle(notification) }
            }
        }
    }
}

class ParallelNoWaitPublishStrategy : PublishStrategy {

    override fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>
    ) {
        notificationHandlers.forEach { CompletableFuture.runAsync { it.handle(notification) } }
    }

    override suspend fun <T : Notification> publishAsync(
        notification: T,
        notificationHandlers: Collection<AsyncNotificationHandler<T>>,
        dispatcher: CoroutineDispatcher
    ) {
        coroutineScope {
            withContext(dispatcher) {
                notificationHandlers.forEach { async { it.handle(notification) } }
            }
        }
    }
}

class ParallelWhenAllPublishStrategy : PublishStrategy {

    override fun <T : Notification> publish(
        notification: T,
        notificationHandlers: Collection<NotificationHandler<T>>
    ) {
        val futures = mutableListOf<CompletableFuture<Void>>()
        notificationHandlers.map { CompletableFuture.runAsync { it.handle(notification) } }
        CompletableFuture.allOf(*futures.toTypedArray()).join()
    }

    override suspend fun <T : Notification> publishAsync(
        notification: T,
        notificationHandlers: Collection<AsyncNotificationHandler<T>>,
        dispatcher: CoroutineDispatcher
    ) {
        coroutineScope {
            withContext(dispatcher) {
                val deferredResults = notificationHandlers.map { async { it.handle(notification) } }
                deferredResults.awaitAll()
            }
        }
    }
}