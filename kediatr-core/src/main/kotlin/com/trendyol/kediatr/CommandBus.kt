package com.trendyol.kediatr

interface CommandBus {
    fun <TQuery : Query<TResponse>, TResponse> executeQuery(query: TQuery): TResponse

    fun <TCommand : Command> executeCommand(command: TCommand)

    /**
     * Publishes the given notification to appropriate notification handlers
     *
     * @since 1.0.9
     * @param T  any [Notification] subclass to publish
     */
    fun <T : Notification> publishNotification(notification: T)

    suspend fun <TQuery : Query<TResponse>, TResponse> executeQueryAsync(query: TQuery): TResponse

    suspend fun <TCommand : Command> executeCommandAsync(command: TCommand)

    /**
     * Publishes the given notification to appropriate notification handlers
     *
     * @since 1.0.9
     * @param T  any [Notification] subclass to publish
     */
    suspend fun <T : Notification> publishNotificationAsync(notification: T)

    fun <TRequest, TResponse> processPipeline(pipelineBehaviors: Collection<PipelineBehavior>, request: TRequest, act: () -> TResponse): TResponse {
        pipelineBehaviors.forEach { it.preProcess(request) }
        val result = act()
        pipelineBehaviors.forEach { it.postProcess(request) }
        return result
    }

    suspend fun <TRequest, TResponse> processAsyncPipeline(pipelineBehaviors: Collection<AsyncPipelineBehavior>, request: TRequest, act: suspend () -> TResponse): TResponse {
        pipelineBehaviors.forEach { it.preProcess(request) }
        val result = act()
        pipelineBehaviors.forEach { it.postProcess(request) }
        return result
    }
}

