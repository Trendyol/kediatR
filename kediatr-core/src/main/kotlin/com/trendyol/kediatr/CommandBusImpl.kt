package com.trendyol.kediatr

import kotlinx.coroutines.*

class CommandBusImpl(private val registery: RegistryImpl) : CommandBus {
    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand, dispatcher: CoroutineDispatcher): Job =
        CoroutineScope(dispatcher).launch {
            registery.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)
        }

    override suspend fun <R, Q : Query<R>> executeQueryAsync(query: Q, dispatcher: CoroutineDispatcher): Deferred<R> =
        CoroutineScope(dispatcher).async {
             registery.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)
        }

    override fun <TCommand : Command> executeCommand(command: TCommand) {
        registery.resolveCommandHandler(command.javaClass).handle(command)
    }

    override fun <R, Q : Query<R>> executeQuery(query: Q): R =
        registery.resolveQueryHandler(query.javaClass).handle(query)

}