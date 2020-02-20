package com.trendyol.kediatr

class CommandBusImpl(private val registry: RegistryImpl) : CommandBus {
    override suspend fun <TCommand : Command> executeCommandAsync(command: TCommand) =
        registry.resolveAsyncCommandHandler(command.javaClass).handleAsync(command)

    override suspend fun <R, Q : Query<R>> executeQueryAsync(query: Q): R =
        registry.resolveAsyncQueryHandler(query.javaClass).handleAsync(query)

    override fun <TCommand : Command> executeCommand(command: TCommand) =
        registry.resolveCommandHandler(command.javaClass).handle(command)

    override fun <R, Q : Query<R>> executeQuery(query: Q): R =
        registry.resolveQueryHandler(query.javaClass).handle(query)

}