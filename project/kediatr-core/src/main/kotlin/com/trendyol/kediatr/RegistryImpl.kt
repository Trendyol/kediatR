@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

class RegistryImpl(
    dependencyProvider: DependencyProvider,
) : Registry {

    private val asyncRegistry = AsyncRegistry(dependencyProvider)

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

    override fun getAsyncPipelineBehaviors(): Collection<AsyncPipelineBehavior> = asyncRegistry.pipelineSet.map { it.get() }
}
