package com.trendyol.kediatr

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class CommandBusImpl(private val registry: RegistryImpl) : CommandBus {
    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand, dispatcher: CoroutineDispatcher) =
        coroutineScope {
            withContext(dispatcher) {
                registry.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)
            }
        }

    override suspend fun <R, Q : Query<R>> executeQueryAsync(query: Q, dispatcher: CoroutineDispatcher): R =
        coroutineScope {
            withContext(dispatcher) {
                registry.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)
            }
        }

    override fun <TCommand : Command> executeCommand(command: TCommand) =
        registry.resolveCommandHandler(command.javaClass).handle(command)

    override fun <R, Q : Query<R>> executeQuery(query: Q): R =
        registry.resolveQueryHandler(query.javaClass).handle(query)

}