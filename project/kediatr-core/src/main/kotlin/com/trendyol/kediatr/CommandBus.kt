package com.trendyol.kediatr

interface CommandBus {

    suspend fun <TQuery : Query<TResponse>, TResponse> executeQueryAsync(query: TQuery): TResponse

    suspend fun <TCommand : Command> executeCommandAsync(command: TCommand)

    suspend fun <TCommand : CommandWithResult<TResult>, TResult> executeCommandAsync(command: TCommand): TResult

    /**
     * Publishes the given notification to appropriate notification handlers
     *
     * @since 1.0.9
     * @param T  any [Notification] subclass to publish
     */
    suspend fun <T : Notification> publishNotificationAsync(notification: T)

    suspend fun <TRequest, TResponse> processAsyncPipeline(
        asyncPipelineBehaviors: Collection<AsyncPipelineBehavior>,
        request: TRequest,
        act: suspend () -> TResponse,
    ): TResponse {
        try {
            asyncPipelineBehaviors.forEach { it.preProcess(request) }
            val result = act()
            asyncPipelineBehaviors.forEach { it.postProcess(request) }
            return result
        } catch (ex: Exception) {
            asyncPipelineBehaviors.forEach { it.handleException(request, ex) }
            throw ex
        }
    }
}