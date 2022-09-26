@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

class RegistryImpl(
    dependencyProvider: DependencyProvider,
) : Registry {

    private val syncRegistry = SyncRegistry(dependencyProvider)
    private val asyncRegistry = AsyncRegistry(dependencyProvider)

    override fun <TCommand : Command> resolveCommandHandler(
        classOfCommand: Class<TCommand>,
    ): CommandHandler<TCommand> {
        val handler = syncRegistry.commandMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as CommandHandler<TCommand>
    }

    override fun <TCommand : CommandWithResult<TResult>, TResult> resolveCommandWithResultHandler(
        classOfCommand: Class<TCommand>,
    ): CommandWithResultHandler<TCommand, TResult> {
        val handler = syncRegistry.commandWithResultMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as CommandWithResultHandler<TCommand, TResult>
    }

    override fun <TNotification : Notification> resolveNotificationHandlers(
        classOfNotification: Class<TNotification>,
    ): Collection<NotificationHandler<TNotification>> =
        syncRegistry.notificationMap.filter { (k, _) -> k.isAssignableFrom(classOfNotification) }
            .flatMap { (_, v) -> v.map { it.get() as NotificationHandler<TNotification> } }

    override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(
        classOfQuery: Class<TQuery>,
    ): QueryHandler<TQuery, TResult> {
        val handler = syncRegistry.queryMap[classOfQuery]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as QueryHandler<TQuery, TResult>
    }

    override fun <TCommand : Command> resolveAsyncCommandHandler(
        classOfCommand: Class<TCommand>,
    ): AsyncCommandHandler<TCommand> {
        val handler = asyncRegistry.commandMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandHandler<TCommand>
    }

    override fun <TCommand : CommandWithResult<TResult>, TResult> resolveAsyncCommandWithResultHandler(
        classOfCommand: Class<TCommand>,
    ): AsyncCommandWithResultHandler<TCommand, TResult> {
        val handler = asyncRegistry.commandWithResultMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandWithResultHandler<TCommand, TResult>
    }

    override fun <TNotification : Notification> resolveAsyncNotificationHandlers(
        classOfNotification: Class<TNotification>,
    ): Collection<AsyncNotificationHandler<TNotification>> =
        asyncRegistry.notificationMap.filter { (k, _) -> k.isAssignableFrom(classOfNotification) }
            .flatMap { (_, v) -> v.map { it.get() as AsyncNotificationHandler<TNotification> } }

    override fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(
        classOfQuery: Class<TQuery>,
    ): AsyncQueryHandler<TQuery, TResult> {
        val handler = asyncRegistry.queryMap[classOfQuery]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as AsyncQueryHandler<TQuery, TResult>
    }

    override fun getPipelineBehaviors(): Collection<PipelineBehavior> = syncRegistry.pipelineSet.map { it.get() }

    override fun getAsyncPipelineBehaviors(): Collection<AsyncPipelineBehavior> = asyncRegistry.pipelineSet.map { it.get() }
}
