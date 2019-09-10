package com.trendyol.kediatr

interface AsyncCommandHandler<TCommand : Command> {
    suspend fun handleAsync(command: TCommand)
}