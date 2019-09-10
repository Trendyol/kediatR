package com.trendyol.kediatr.spring

import com.trendyol.kediatr.*
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import java.util.*

class SpringBeanRegistry(applicationContext: ApplicationContext) : Registry {
    private val commandMap = HashMap<Class<out Command>, CommandProvider<CommandHandler<*>>>()
    private val queryMap = HashMap<Class<out Query<*>>, QueryProvider<QueryHandler<*, *>>>()
    private val asyncCommandMap = HashMap<Class<out Command>, AsyncCommandProvider<AsyncCommandHandler<*>>>()
    private val asyncQueryMap = HashMap<Class<out Query<*>>, AsyncQueryProvider<AsyncQueryHandler<*, *>>>()

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
            val queryType = generics!![1] as Class<Query<*>>
            queryMap[queryType] = QueryProvider(applicationContext, handlerClass)
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
            val queryType = generics!![1] as Class<Query<*>>
            asyncQueryMap[queryType] = AsyncQueryProvider(applicationContext, handlerClass)
        }
    }

    override fun <TCommand : Command> resolveCommandHandler(commandClass: Class<TCommand>): CommandHandler<TCommand> {
        val handler = commandMap[commandClass]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${commandClass.name}")
        return handler as CommandHandler<TCommand>
    }

    override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TResult, TQuery> {
        val handler = queryMap[classOfQuery]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as QueryHandler<TResult, TQuery>
    }

    override fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand> {
        val handler = asyncCommandMap[classOfCommand]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandHandler<TCommand>
    }

    override fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TResult, TQuery> {
        val handler = asyncQueryMap[classOfQuery]?.get()
            ?: throw HandlerBeanNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as AsyncQueryHandler<TResult, TQuery>
    }

}