package com.trendyol.kediatr

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

interface CommandBus {
    fun <TCommand : Command> executeCommand(command: TCommand)

    fun <R, Q : Query<R>> executeQuery(query: Q): R

    suspend fun <TCommand : Command> executeCommandAsync(command: TCommand, dispatcher: CoroutineDispatcher = Dispatchers.IO) : Job

    suspend fun <R, Q : Query<R>> executeQueryAsync(query: Q, dispatcher: CoroutineDispatcher = Dispatchers.IO): Deferred<R>
}

