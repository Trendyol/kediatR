package com.trendyol.kediatr

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface PublishStrategy {
  suspend fun <T : Notification> publish(
    notification: T,
    notificationHandlers: Collection<NotificationHandler<T>>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
  )
}
