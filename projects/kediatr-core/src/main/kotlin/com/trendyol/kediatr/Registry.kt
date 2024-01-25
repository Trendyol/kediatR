package com.trendyol.kediatr

interface Registry {
    fun <TCommand : Command> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand>

    fun <TCommand : CommandWithResult<TResult>, TResult> resolveCommandWithResultHandler(
        classOfCommand: Class<TCommand>
    ): CommandWithResultHandler<TCommand, TResult>

    fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery, TResult>

    fun <TNotification : Notification> resolveNotificationHandlers(
        classOfNotification: Class<TNotification>
    ): Collection<NotificationHandler<TNotification>>

    fun getPipelineBehaviors(): Collection<PipelineBehavior>
}
