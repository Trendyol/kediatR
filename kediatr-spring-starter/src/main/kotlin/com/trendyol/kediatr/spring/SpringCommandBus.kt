package com.trendyol.kediatr.spring

import com.trendyol.kediatr.*

class SpringCommandBus(
    private val springBeanRegistry: SpringBeanRegistry,
    private val publishStrategy: PublishStrategy = StopOnExceptionPublishStrategy()
) : CommandBus {

    override fun <TCommand : Command> executeCommand(command: TCommand) = processPipeline(springBeanRegistry.getPipelineBehaviors(), command) {
        springBeanRegistry.resolveCommandHandler(command.javaClass).handle(command)
    }

    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand) = processAsyncPipeline(springBeanRegistry.getAsyncPipelineBehaviors(), command) {
        springBeanRegistry.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)
    }

    override fun <Q : Query<R>, R> executeQuery(query: Q): R = processPipeline(springBeanRegistry.getPipelineBehaviors(), query) {
        springBeanRegistry.resolveQueryHandler(query.javaClass).handle(query)
    }

    override suspend fun <Q : Query<R>, R> executeQueryAsync(query: Q): R = processAsyncPipeline(springBeanRegistry.getAsyncPipelineBehaviors(), query) {
        springBeanRegistry.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)
    }

    override fun <T : Notification> publishNotification(notification: T) = processPipeline(springBeanRegistry.getPipelineBehaviors(), notification) {
        publishStrategy.publish(notification, springBeanRegistry.resolveNotificationHandlers(notification.javaClass))
    }

    override suspend fun <T : Notification> publishNotificationAsync(notification: T) = processAsyncPipeline(springBeanRegistry.getAsyncPipelineBehaviors(), notification) {
        publishStrategy.publishAsync(
            notification,
            springBeanRegistry.resolveAsyncNotificationHandlers(notification.javaClass)
        )
    }
}