package com.trendyol.kediatr.spring

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.Query
import kotlinx.coroutines.*

class SpringCommandBus(private val springBeanRegistry: SpringBeanRegistry) : CommandBus {

    override suspend fun <R, Q : Query<R>> executeQueryAsync(query: Q, dispatcher: CoroutineDispatcher): R =
        coroutineScope {
            withContext(dispatcher) {
                springBeanRegistry.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)
            }
        }

    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand, dispatcher: CoroutineDispatcher) =
        coroutineScope {
            withContext(dispatcher){
                springBeanRegistry.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)
            }
        }

    override fun <R, Q : Query<R>> executeQuery(query: Q): R =
        springBeanRegistry.resolveQueryHandler(query.javaClass).handle(query)

    override fun <TCommand : Command> executeCommand(command: TCommand) {
        springBeanRegistry.resolveCommandHandler(command.javaClass).handle(command)
    }
}