package com.trendyol.kediatr

interface CommandBus {
    fun <R, Q : Query<R>> executeQuery(query: Q): R

    fun <TCommand : Command> executeCommand(command: TCommand)

    /**
     * Publishes the given notification to appropriate notification handlers
     *
     * @since 1.0.9
     * @param T  any [Notification] subclass to publish
     */
    fun <T : Notification> publishNotification(notification: T)

    suspend fun <R, Q : Query<R>> executeQueryAsync(query: Q): R

    suspend fun <TCommand : Command> executeCommandAsync(command: TCommand)

    /**
     * Publishes the given notification to appropriate notification handlers
     *
     * @since 1.0.9
     * @param T  any [Notification] subclass to publish
     */
    suspend fun <T : Notification> publishNotificationAsync(notification: T)
}

