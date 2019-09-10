package com.trendyol.kediatr

import org.reflections.Reflections
import java.lang.reflect.ParameterizedType
import java.util.*


class RegistryImpl(private val anyClazz: Class<*>) : Registry {

    private val commandMap = HashMap<Class<out Command>, CommandHandler<*>>()
    private val queryMap = HashMap<Class<out Query<*>>, QueryHandler<*, *>>()
    private val asyncCommandMap = HashMap<Class<out Command>, AsyncCommandHandler<*>>()
    private val asyncQueryMap = HashMap<Class<out Query<*>>, AsyncQueryHandler<*, *>>()

    init {
        val reflections = Reflections(anyClazz.`package`.name)
        reflections.getSubTypesOf(QueryHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType)
                    && (genericInterface as ParameterizedType).rawType as Class<*> == QueryHandler::class.java
                ) {
                    val queryClazz = (genericInterface as ParameterizedType)
                        .actualTypeArguments[1];

                    queryMap[queryClazz as Class<out Query<*>>] =
                        (it as Class<out QueryHandler<*, *>>).newInstance() as QueryHandler<*, *>
                }
            }
        }

        reflections.getSubTypesOf(CommandHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType)
                    && (genericInterface as ParameterizedType).rawType as Class<*> == CommandHandler::class.java
                ) {
                    val commandClazz = (genericInterface as ParameterizedType)
                        .actualTypeArguments[0];

                    commandMap[commandClazz as Class<out Command>] =
                        (it as Class<out CommandHandler<*>>).newInstance() as CommandHandler<*>
                }
            }
        }
        reflections.getSubTypesOf(AsyncQueryHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType)
                    && (genericInterface as ParameterizedType).rawType as Class<*> == AsyncQueryHandler::class.java
                ) {
                    val queryClazz = (genericInterface as ParameterizedType)
                        .actualTypeArguments[1];

                    asyncQueryMap[queryClazz as Class<out Query<*>>] =
                        (it as Class<out AsyncQueryHandler<*, *>>).newInstance() as AsyncQueryHandler<*, *>
                }
            }
        }

        reflections.getSubTypesOf(AsyncCommandHandler::class.java).forEach {
            (it.genericInterfaces).forEach { genericInterface ->
                if ((genericInterface is ParameterizedType)
                    && (genericInterface as ParameterizedType).rawType as Class<*> == AsyncCommandHandler::class.java
                ) {
                    val commandClazz = (genericInterface as ParameterizedType)
                        .actualTypeArguments[0];

                    asyncCommandMap[commandClazz as Class<out Command>] =
                        (it as Class<out AsyncCommandHandler<*>>).newInstance() as AsyncCommandHandler<*>
                }
            }
        }
    }

    override fun <TCommand : Command> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand> {
        val handler = commandMap[classOfCommand]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as CommandHandler<TCommand>;
    }

    override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TResult, TQuery> {
        val handler = queryMap[classOfQuery]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as QueryHandler<TResult, TQuery>;
    }

    override fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand> {
        val handler = asyncCommandMap[classOfCommand]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandHandler<TCommand>;
    }

    override fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TResult, TQuery> {
        val handler = asyncQueryMap[classOfQuery]
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as AsyncQueryHandler<TResult, TQuery>;
    }

}

