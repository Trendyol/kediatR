package com.trendyol.kediatr

import com.trendyol.kediatr.common.AsyncNotificationProvider
import com.trendyol.kediatr.common.AsyncPipelineProvider
import com.trendyol.kediatr.common.AsyncQueryProvider

@Suppress("UNCHECKED_CAST")
internal class AsyncRegistry(dependencyProvider: DependencyProvider) : Registrar() {
    val commandMap = HashMap<Class<*>, AsyncCommandProvider<AsyncCommandHandler<Command>>>()
    val queryMap = HashMap<Class<*>, AsyncQueryProvider<AsyncQueryHandler<*, *>>>()
    val notificationMap =
        HashMap<Class<*>, MutableList<AsyncNotificationProvider<AsyncNotificationHandler<*>>>>()
    val pipelineSet = HashSet<AsyncPipelineProvider<*>>()
    val commandWithResultMap =
        HashMap<Class<*>, AsyncCommandWithResultProvider<*>>()

    init {

        registerFor<AsyncQueryHandler<Query<*>, *>, Query<*>>(dependencyProvider) { key, value ->
            queryMap[key] = AsyncQueryProvider(dependencyProvider, value as Class<AsyncQueryHandler<*, *>>)
        }

        registerFor<AsyncCommandHandler<Command>, Command>(dependencyProvider) { key, value ->
            commandMap[key] = AsyncCommandProvider(dependencyProvider, value)
        }

        registerFor<AsyncCommandWithResultHandler<CommandWithResult<*>, *>, CommandWithResult<*>>(dependencyProvider) { key, value ->
            commandWithResultMap[key] = AsyncCommandWithResultProvider(
                dependencyProvider,
                value as Class<AsyncCommandWithResultHandler<*, *>>
            )
        }

        registerFor<AsyncNotificationHandler<Notification>, Notification>(dependencyProvider) { key, value ->
            notificationMap.getOrPut(key) { mutableListOf() }
                .add(AsyncNotificationProvider(dependencyProvider, value as Class<AsyncNotificationHandler<*>>))
        }

        registerFor<AsyncPipelineBehavior>(dependencyProvider) { handler ->
            pipelineSet.add(AsyncPipelineProvider(dependencyProvider, handler))
        }
    }
}
