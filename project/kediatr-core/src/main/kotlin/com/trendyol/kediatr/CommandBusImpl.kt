package com.trendyol.kediatr

class CommandBusImpl(
    private val registry: Registry,
    private val publishStrategy: PublishStrategy = StopOnExceptionPublishStrategy(),
) : CommandBus {

    override suspend fun <TQuery : Query<TResponse>, TResponse> executeQueryAsync(query: TQuery): TResponse = processAsyncPipeline(
        registry.getAsyncPipelineBehaviors(),
        query
    ) {
        registry.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)
    }

    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand) = processAsyncPipeline(
        registry.getAsyncPipelineBehaviors(),
        command
    ) {
        registry.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)
    }

    override suspend fun <TCommand : CommandWithResult<TResult>, TResult> executeCommandAsync(command: TCommand): TResult =
        processAsyncPipeline(
            registry.getAsyncPipelineBehaviors(),
            command
        ) {
            registry.resolveAsyncCommandWithResultHandler(command.javaClass).handleAsync(command)
        }

    override suspend fun <T : Notification> publishNotificationAsync(notification: T) = processAsyncPipeline(
        registry.getAsyncPipelineBehaviors(),
        notification
    ) {
        publishStrategy.publishAsync(notification, registry.resolveAsyncNotificationHandlers(notification.javaClass))
    }
}
