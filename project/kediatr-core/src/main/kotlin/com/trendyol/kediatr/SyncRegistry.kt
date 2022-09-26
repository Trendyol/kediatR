package com.trendyol.kediatr

import com.trendyol.kediatr.common.NotificationProvider
import com.trendyol.kediatr.common.PipelineProvider
import com.trendyol.kediatr.common.QueryProvider

@Suppress("UNCHECKED_CAST")
internal class SyncRegistry(dependencyProvider: DependencyProvider) : Registrar() {
    val queryMap = HashMap<Class<*>, QueryProvider<QueryHandler<*, *>>>()
    val commandMap = HashMap<Class<*>, CommandProvider<CommandHandler<Command>>>()
    val notificationMap = HashMap<Class<*>, MutableList<NotificationProvider<NotificationHandler<*>>>>()
    val pipelineSet = HashSet<PipelineProvider<*>>()
    val commandWithResultMap = HashMap<Class<*>, CommandWithResultProvider<CommandWithResultHandler<*, *>>>()

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

        registerFor<PipelineBehavior>(dependencyProvider) { handler ->
            pipelineSet.add(PipelineProvider(dependencyProvider, handler))
        }
    }
}
