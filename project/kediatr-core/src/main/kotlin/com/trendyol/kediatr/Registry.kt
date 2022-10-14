package com.trendyol.kediatr

interface Registry {

    fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand>

    fun <TCommand : CommandWithResult<TResult>, TResult> resolveAsyncCommandWithResultHandler(
        classOfCommand: Class<TCommand>,
    ): AsyncCommandWithResultHandler<TCommand, TResult>

    fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(
        classOfQuery: Class<TQuery>,
    ): AsyncQueryHandler<TQuery, TResult>

    fun <TNotification : Notification> resolveAsyncNotificationHandlers(
        classOfNotification: Class<TNotification>,
    ): Collection<AsyncNotificationHandler<TNotification>>

    fun getAsyncPipelineBehaviors(): Collection<AsyncPipelineBehavior>
}
