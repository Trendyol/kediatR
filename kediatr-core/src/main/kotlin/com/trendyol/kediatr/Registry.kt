package com.trendyol.kediatr


interface Registry {
    fun <TCommand : Command> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand>

    fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery,TResult>

    fun <TNotification : Notification> resolveNotificationHandlers(classOfNotification: Class<TNotification>): Collection<NotificationHandler<TNotification>>

    fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand>

    fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TQuery,TResult>

    fun <TNotification : Notification> resolveAsyncNotificationHandlers(classOfNotification: Class<TNotification>): Collection<AsyncNotificationHandler<TNotification>>

    fun getPipelineBehaviors(): Collection<PipelineBehavior>

    fun getAsyncPipelineBehaviors(): Collection<AsyncPipelineBehavior>
}