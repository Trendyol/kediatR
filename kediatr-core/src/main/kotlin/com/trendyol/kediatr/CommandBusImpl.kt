package com.trendyol.kediatr

class CommandBusImpl(private val registry: RegistryImpl, private val publishStrategy: PublishStrategy) : CommandBus {
    override fun <R, Q : Query<R>> executeQuery(query: Q): R =
        registry.resolveQueryHandler(query.javaClass).handle(query)

    override fun <TCommand : Command> executeCommand(command: TCommand) =
        registry.resolveCommandHandler(command.javaClass).handle(command)

    override fun <T : Notification> publishNotification(notification: T) =
        publishStrategy.publish(notification, registry.resolveNotificationHandlers(notification.javaClass))

    override suspend fun <R, Q : Query<R>> executeQueryAsync(query: Q): R =
        registry.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)

    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand) =
        registry.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)

    override suspend fun <T : Notification> publishNotificationAsync(notification: T) =
        publishStrategy.publishAsync(notification, registry.resolveAsyncNotificationHandlers(notification.javaClass))
}