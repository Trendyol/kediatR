package com.trendyol.kediatr

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface PublishStrategy {

    suspend fun <T : Notification> publishAsync(
        notification: T,
        notificationHandlers: Collection<AsyncNotificationHandler<T>>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    )
}
