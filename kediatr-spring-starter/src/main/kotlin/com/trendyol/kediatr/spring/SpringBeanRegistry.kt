package com.trendyol.kediatr.spring

import com.trendyol.kediatr.*
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import java.util.*

class SpringBeanRegistry(applicationContext: ApplicationContext) : Registry {
    private val commandMap = HashMap<Class<out Command>, CommandProvider<CommandHandler<*>>>()
    private val queryMap = HashMap<Class<out Query<*>>, QueryProvider<QueryHandler<*, *>>>()
    private val notificationMap =
        HashMap<Class<out Notification>, MutableList<NotificationProvider<NotificationHandler<*>>>>()
    private val pipelineSet = HashSet<PipelineProvider<PipelineBehavior>>()


    private val asyncCommandMap = HashMap<Class<out Command>, AsyncCommandProvider<AsyncCommandHandler<*>>>()
    private val asyncQueryMap = HashMap<Class<out Query<*>>, AsyncQueryProvider<AsyncQueryHandler<*, *>>>()
    private val asyncNotificationMap =
        HashMap<Class<out Notification>, MutableList<AsyncNotificationProvider<AsyncNotificationHandler<*>>>>()
    private val asyncPipelineSet = HashSet<AsyncPipelineProvider<AsyncPipelineBehavior>>()

    init {
        val commandNames = applicationContext.getBeanNamesForType(CommandHandler::class.java)
        for (name in commandNames) {
            val handlerClass = applicationContext.getType(name) as Class<CommandHandler<*>>
            val generics = GenericTypeResolver.resolveTypeArguments(handlerClass, CommandHandler::class.java)
            val commandType = generics!![0] as Class<Command>
            commandMap[commandType] = CommandProvider(applicationContext, handlerClass)
        }

        val queryNames = applicationContext.getBeanNamesForType(QueryHandler::class.java)
        for (name in queryNames) {
            val handlerClass = applicationContext.getType(name) as Class<QueryHandler<*, *>>
            val generics = GenericTypeResolver.resolveTypeArguments(handlerClass, QueryHandler::class.java)
            val queryType = generics!![0] as Class<Query<*>>
            queryMap[queryType] = QueryProvider(applicationContext, handlerClass)
        }

        val notificationHandlerNames = applicationContext.getBeanNamesForType(NotificationHandler::class.java)
        for (name in notificationHandlerNames) {
            val handlerClass = applicationContext.getType(name) as Class<NotificationHandler<*>>
            val generics = GenericTypeResolver.resolveTypeArguments(handlerClass, NotificationHandler::class.java)
            val notificationType = generics!![0] as Class<Notification>
            notificationMap.getOrPut(notificationType) { mutableListOf() }
                .add(NotificationProvider(applicationContext, handlerClass))
        }

        val asyncCommandNames = applicationContext.getBeanNamesForType(AsyncCommandHandler::class.java)
        for (name in asyncCommandNames) {
            val handlerClass = applicationContext.getType(name) as Class<AsyncCommandHandler<*>>
            val generics = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncCommandHandler::class.java)
            val commandType = generics!![0] as Class<Command>
            asyncCommandMap[commandType] = AsyncCommandProvider(applicationContext, handlerClass)
        }

        val asyncQueryNames = applicationContext.getBeanNamesForType(AsyncQueryHandler::class.java)
        for (name in asyncQueryNames) {
            val handlerClass = applicationContext.getType(name) as Class<AsyncQueryHandler<*, *>>
            val generics = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncQueryHandler::class.java)
            val queryType = generics!![0] as Class<Query<*>>
            asyncQueryMap[queryType] = AsyncQueryProvider(applicationContext, handlerClass)
        }

        val asyncNotificationHandlerNames = applicationContext.getBeanNamesForType(AsyncNotificationHandler::class.java)
        for (name in asyncNotificationHandlerNames) {
            val handlerClass = applicationContext.getType(name) as Class<AsyncNotificationHandler<*>>
            val generics = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncNotificationHandler::class.java)
            val notificationType = generics!![0] as Class<Notification>
            asyncNotificationMap.getOrPut(notificationType) { mutableListOf() }
                .add(AsyncNotificationProvider(applicationContext, handlerClass))
        }

        val pipelineBehaviorNames = applicationContext.getBeanNamesForType(PipelineBehavior::class.java)
        for (name in pipelineBehaviorNames) {
            val pipelineBehaviorClazz = applicationContext.getType(name) as Class<PipelineBehavior>
            pipelineSet.add(PipelineProvider(applicationContext, pipelineBehaviorClazz))
        }

        val asyncPipelineBehaviorNames = applicationContext.getBeanNamesForType(AsyncPipelineBehavior::class.java)
        for (name in asyncPipelineBehaviorNames) {
            val asyncPipelineBehaviorClazz = applicationContext.getType(name) as Class<AsyncPipelineBehavior>
            asyncPipelineSet.add(AsyncPipelineProvider(applicationContext, asyncPipelineBehaviorClazz))
        }
    }

    override fun <TCommand : Command> resolveCommandHandler(commandClass: Class<TCommand>): CommandHandler<TCommand> {
        val handler = commandMap[commandClass]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${commandClass.name}")
        return handler as CommandHandler<TCommand>
    }

    override fun <TNotification : Notification> resolveNotificationHandlers(classOfNotification: Class<TNotification>): Collection<NotificationHandler<TNotification>> {
        val notificationHandlers = mutableListOf<NotificationHandler<TNotification>>()
        notificationMap.forEach { (k, v) ->
            if (k.isAssignableFrom(classOfNotification)) {
                v.forEach { notificationHandlers.add(it.get() as NotificationHandler<TNotification>) }
            }
        }
        return notificationHandlers
    }

    override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery, TResult> {
        val handler = queryMap[classOfQuery]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as QueryHandler<TQuery, TResult>
    }

    override fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand> {
        val handler = asyncCommandMap[classOfCommand]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandHandler<TCommand>
    }

    override fun <TNotification : Notification> resolveAsyncNotificationHandlers(classOfNotification: Class<TNotification>): Collection<AsyncNotificationHandler<TNotification>> {
        val asyncNotificationHandlers = mutableListOf<AsyncNotificationHandler<TNotification>>()
        asyncNotificationMap.forEach { (k, v) ->
            if (k.isAssignableFrom(classOfNotification)) {
                v.forEach { asyncNotificationHandlers.add(it.get() as AsyncNotificationHandler<TNotification>) }
            }
        }
        return asyncNotificationHandlers
    }

    override fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TQuery, TResult> {
        val handler = asyncQueryMap[classOfQuery]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as AsyncQueryHandler<TQuery, TResult>
    }

    override fun getPipelineBehaviors(): Collection<PipelineBehavior> {
        return pipelineSet.map { it.get() }
    }

    override fun getAsyncPipelineBehaviors(): Collection<AsyncPipelineBehavior> {
        return asyncPipelineSet.map { it.get() }
    }
}