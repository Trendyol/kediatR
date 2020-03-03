package com.trendyol.kediatr

import org.reflections.Reflections
import java.lang.reflect.ParameterizedType
import java.util.*

class RegistryImpl(anyClazz: Class<*>) : Registry {

    private val commandMap = HashMap<Class<out Command>, CommandHandler<*>>()
    private val queryMap = HashMap<Class<out Query<*>>, QueryHandler<*, *>>()
    private val notificationMap = HashMap<Class<out Notification>, MutableCollection<NotificationHandler<*>>>()

    private val asyncCommandMap = HashMap<Class<out Command>, AsyncCommandHandler<*>>()
    private val asyncQueryMap = HashMap<Class<out Query<*>>, AsyncQueryHandler<*, *>>()
    private val asyncNotificationMap =
        HashMap<Class<out Notification>, MutableCollection<AsyncNotificationHandler<*>>>()

    init {
        val reflections = Reflections(anyClazz.`package`.name)
        reflections.getSubTypesOf(QueryHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == QueryHandler::class.java) {
                    val queryClazz = genericInterface.actualTypeArguments[1]

                    queryMap[queryClazz as Class<out Query<*>>] =
                        (it as Class<out QueryHandler<*, *>>).newInstance() as QueryHandler<*, *>
                }
            }
        }

        reflections.getSubTypesOf(CommandHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == CommandHandler::class.java) {
                    val commandClazz = genericInterface.actualTypeArguments[0]

                    commandMap[commandClazz as Class<out Command>] =
                        (it as Class<out CommandHandler<*>>).newInstance() as CommandHandler<*>
                }
            }
        }

        reflections.getSubTypesOf(NotificationHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == NotificationHandler::class.java) {
                    val notificationClazz = genericInterface.actualTypeArguments.first() as Class<out Notification>
                    val asyncNotificationHandler =
                        (it as Class<out NotificationHandler<*>>).newInstance() as NotificationHandler<*>

                    notificationMap.getOrPut(notificationClazz) { mutableListOf() }
                        .add(asyncNotificationHandler)
                }
            }
        }

        reflections.getSubTypesOf(AsyncQueryHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == AsyncQueryHandler::class.java) {
                    val queryClazz = genericInterface.actualTypeArguments[1]

                    asyncQueryMap[queryClazz as Class<out Query<*>>] =
                        (it as Class<out AsyncQueryHandler<*, *>>).newInstance() as AsyncQueryHandler<*, *>
                }
            }
        }

        reflections.getSubTypesOf(AsyncCommandHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == AsyncCommandHandler::class.java) {
                    val commandClazz = genericInterface.actualTypeArguments[0]

                    asyncCommandMap[commandClazz as Class<out Command>] =
                        (it as Class<out AsyncCommandHandler<*>>).newInstance() as AsyncCommandHandler<*>
                }
            }
        }

        reflections.getSubTypesOf(AsyncNotificationHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType) && genericInterface.rawType as Class<*> == AsyncNotificationHandler::class.java) {
                    val notificationClazz = genericInterface.actualTypeArguments.first() as Class<out Notification>
                    val asyncNotificationHandler =
                        (it as Class<out AsyncNotificationHandler<*>>).newInstance() as AsyncNotificationHandler<*>

                    asyncNotificationMap.getOrPut(notificationClazz) { mutableListOf() }
                        .add(asyncNotificationHandler)
                }
            }
        }
    }

    override fun <TCommand : Command> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand> {
        val handler = commandMap[classOfCommand]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as CommandHandler<TCommand>
    }

    override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TResult, TQuery> {
        val handler = queryMap[classOfQuery]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as QueryHandler<TResult, TQuery>
    }

    override fun <TNotification : Notification> resolveNotificationHandlers(classOfNotification: Class<TNotification>): Collection<NotificationHandler<TNotification>> {
        val notificationHandlers = mutableListOf<NotificationHandler<TNotification>>()
        notificationMap.forEach { (k, v) ->
            if (k.isAssignableFrom(classOfNotification)) {
                v.forEach { notificationHandlers.add(it as NotificationHandler<TNotification>) }
            }
        }
        return notificationHandlers
    }

    override fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand> {
        val handler = asyncCommandMap[classOfCommand]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandHandler<TCommand>
    }

    override fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TResult, TQuery> {
        val handler = asyncQueryMap[classOfQuery]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as AsyncQueryHandler<TResult, TQuery>
    }

    override fun <TNotification : Notification> resolveAsyncNotificationHandlers(classOfNotification: Class<TNotification>): Collection<AsyncNotificationHandler<TNotification>> {
        val asyncNotificationHandlers = mutableListOf<AsyncNotificationHandler<TNotification>>()
        asyncNotificationMap.forEach { (k, v) ->
            if (k.isAssignableFrom(classOfNotification)) {
                v.forEach { asyncNotificationHandlers.add(it as AsyncNotificationHandler<TNotification>) }
            }
        }
        return asyncNotificationHandlers
    }
}

