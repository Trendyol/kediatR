package com.trendyol.kediatr

/**
 * Interface to be implemented for a blocking notification handler
 *
 * @since 1.0.9
 * @param T any [Notification] subclass to handle
 * @see Notification
 * @see NotificationHandler
 */
interface NotificationHandler<in T> where T : Notification {

    /**
     * Handles a notification
     *
     * @param notification the notification to handle
     */
    fun handle(notification: T)
}