@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

import com.trendyol.kediatr.common.*
import java.lang.reflect.ParameterizedType

class RegistryImpl(
    dependencyProvider: DependencyProvider
) : Registry {
    private val queryMap = HashMap<Class<*>, QueryProvider<QueryHandler<*, *>>>()
    private val commandMap = HashMap<Class<*>, CommandProvider<CommandHandler<Command>>>()
    private val notificationMap = HashMap<Class<*>, MutableList<NotificationProvider<NotificationHandler<*>>>>()
    private val pipelineSet = HashSet<PipelineProvider<*>>()
    private val commandWithResultMap = HashMap<Class<*>, CommandWithResultProvider<CommandWithResultHandler<*, *>>>()

    private val asyncCommandMap = HashMap<Class<*>, AsyncCommandProvider<AsyncCommandHandler<Command>>>()
    private val asyncQueryMap = HashMap<Class<*>, AsyncQueryProvider<AsyncQueryHandler<*, *>>>()
    private val asyncNotificationMap =
        HashMap<Class<*>, MutableList<AsyncNotificationProvider<AsyncNotificationHandler<*>>>>()
    private val asyncPipelineSet = HashSet<AsyncPipelineProvider<*>>()
    private val asyncCommandWithResultMap =
        HashMap<Class<*>, AsyncCommandWithResultProvider<*>>()

    init {

        registerFor<QueryHandler<Query<*>, *>, Query<*>>(dependencyProvider) { key, value ->
            queryMap[key] = QueryProvider(dependencyProvider, value as Class<QueryHandler<*, *>>)
        }

        registerFor<CommandHandler<Command>, Command>(dependencyProvider) { key, value ->
            commandMap[key] = CommandProvider(dependencyProvider, value)
        }

        registerFor<CommandWithResultHandler<CommandWithResult<*>, *>, CommandWithResult<*>>(dependencyProvider) { key, value ->
            commandWithResultMap[key] =
                CommandWithResultProvider(dependencyProvider, value as Class<CommandWithResultHandler<*, *>>)
        }

        registerFor<NotificationHandler<Notification>, Notification>(dependencyProvider) { key, value ->
            notificationMap.getOrPut(key) { mutableListOf() }
                .add(NotificationProvider(dependencyProvider, value as Class<NotificationHandler<*>>))
        }

        registerFor<AsyncQueryHandler<Query<*>, *>, Query<*>>(dependencyProvider) { key, value ->
            asyncQueryMap[key] = AsyncQueryProvider(dependencyProvider, value as Class<AsyncQueryHandler<*, *>>)
        }

        registerFor<AsyncCommandHandler<Command>, Command>(dependencyProvider) { key, value ->
            asyncCommandMap[key] = AsyncCommandProvider(dependencyProvider, value)
        }

        registerFor<AsyncCommandWithResultHandler<CommandWithResult<*>, *>, CommandWithResult<*>>(dependencyProvider) { key, value ->
            asyncCommandWithResultMap[key] = AsyncCommandWithResultProvider(
                dependencyProvider,
                value as Class<AsyncCommandWithResultHandler<*, *>>
            )
        }

        registerFor<AsyncNotificationHandler<Notification>, Notification>(dependencyProvider) { key, value ->
            asyncNotificationMap.getOrPut(key) { mutableListOf() }
                .add(AsyncNotificationProvider(dependencyProvider, value as Class<AsyncNotificationHandler<*>>))
        }

        registerFor<AsyncPipelineBehavior>(dependencyProvider) { handler ->
            asyncPipelineSet.add(AsyncPipelineProvider(dependencyProvider, handler))
        }

        registerFor<PipelineBehavior>(dependencyProvider) { handler ->
            pipelineSet.add(PipelineProvider(dependencyProvider, handler))
        }
    }

    private inline fun <reified T> registerFor(
        dependencyProvider: DependencyProvider,
        registrar: (value: Class<T>) -> Unit
    ) = dependencyProvider.getSubTypesOf(T::class.java).forEach { handler ->
        registerFor<T>(handler) { value -> registrar(value as Class<T>) }
    }

    private inline fun <reified T> registerFor(
        handler: Class<*>,
        registrar: (value: Class<*>) -> Unit
    ) {
        val interfaceOrBaseClass = T::class.java
        if (!interfaceOrBaseClass.isAssignableFrom(handler)) return
        registrar(handler)
    }

    private inline fun <reified THandler : Any, TParameter> registerFor(
        dependencyProvider: DependencyProvider,
        registrar: (key: Class<TParameter>, value: Class<THandler>) -> Unit
    ) = dependencyProvider.getSubTypesOf(THandler::class.java).forEach {
        registerFor<THandler, TParameter>(it) { key, value ->
            registrar(key as Class<TParameter>, value as Class<THandler>)
        }
    }

    private inline fun <reified THandler : Any, TParameter> registerFor(
        handler: Class<*>,
        registrar: (key: Class<*>, value: Class<*>) -> Unit
    ) {
        val interfaceOrBaseClass = THandler::class.java
        if (!interfaceOrBaseClass.isAssignableFrom(handler)) return

        handler.genericInterfaces
            .filterIsInstance<ParameterizedType>()
            .map { extractParameter<TParameter>(it) }
            .forEach { registrar(it, handler) }

        when (handler.genericSuperclass) {
            is ParameterizedType -> {
                val inheritedHandler = (handler.genericSuperclass as ParameterizedType).rawType as Class<*>
                inheritedHandler.genericInterfaces
                    .filterIsInstance<ParameterizedType>()
                    .map { extractParameter<TParameter>(handler.genericSuperclass as ParameterizedType) }
                    .forEach { registrar(it, handler) }
            }

            is Class<*> -> {
                val inheritedHandler = (handler.genericSuperclass as Class<*>)
                if (interfaceOrBaseClass.isAssignableFrom(inheritedHandler)) {
                    inheritedHandler.genericInterfaces
                        .filterIsInstance<ParameterizedType>()
                        .map { extractParameter<TParameter>(it) }
                        .forEach { registrar(it, handler) }
                }
            }
        }
    }

    private fun <T> extractParameter(genericInterface: ParameterizedType): Class<out T> =
        when (val typeArgument = genericInterface.actualTypeArguments[0]) {
            is ParameterizedType -> typeArgument.rawType as Class<out T>
            else -> typeArgument as Class<out T>
        }

    override fun <TCommand : Command> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand> {
        val handler = commandMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as CommandHandler<TCommand>
    }

    override fun <TCommand : CommandWithResult<TResult>, TResult> resolveCommandWithResultHandler(classOfCommand: Class<TCommand>): CommandWithResultHandler<TCommand, TResult> {
        val handler = commandWithResultMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as CommandWithResultHandler<TCommand, TResult>
    }

    override fun <TNotification : Notification> resolveNotificationHandlers(classOfNotification: Class<TNotification>): Collection<NotificationHandler<TNotification>> =
        notificationMap.filter { (k, _) -> k.isAssignableFrom(classOfNotification) }
            .flatMap { (_, v) -> v.map { it.get() as NotificationHandler<TNotification> } }

    override fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TQuery, TResult> {
        val handler = queryMap[classOfQuery]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as QueryHandler<TQuery, TResult>
    }

    override fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand> {
        val handler = asyncCommandMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandHandler<TCommand>
    }

    override fun <TCommand : CommandWithResult<TResult>, TResult> resolveAsyncCommandWithResultHandler(classOfCommand: Class<TCommand>): AsyncCommandWithResultHandler<TCommand, TResult> {
        val handler = asyncCommandWithResultMap[classOfCommand]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfCommand.name}")
        return handler as AsyncCommandWithResultHandler<TCommand, TResult>
    }

    override fun <TNotification : Notification> resolveAsyncNotificationHandlers(classOfNotification: Class<TNotification>): Collection<AsyncNotificationHandler<TNotification>> =
        asyncNotificationMap.filter { (k, _) -> k.isAssignableFrom(classOfNotification) }
            .flatMap { (_, v) -> v.map { it.get() as AsyncNotificationHandler<TNotification> } }

    override fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TQuery, TResult> {
        val handler = asyncQueryMap[classOfQuery]?.get()
            ?: throw HandlerNotFoundException("handler could not be found for ${classOfQuery.name}")
        return handler as AsyncQueryHandler<TQuery, TResult>
    }

    override fun getPipelineBehaviors(): Collection<PipelineBehavior> = pipelineSet.map { it.get() }

    override fun getAsyncPipelineBehaviors(): Collection<AsyncPipelineBehavior> = asyncPipelineSet.map { it.get() }
}
