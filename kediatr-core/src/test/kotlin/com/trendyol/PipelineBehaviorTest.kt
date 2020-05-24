package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

var asyncPipelinePreProcessCounter = 0
var asyncPipelinePostProcessCounter = 0
var pipelinePreProcessCounter = 0
var pipelinePostProcessCounter = 0
var pipelineExceptionCounter = 0
var asyncPipelineExceptionCounter = 0

class PipelineBehaviorTest {

    init {
        asyncPipelinePreProcessCounter = 0
        asyncPipelinePostProcessCounter = 0
        pipelinePreProcessCounter = 0
        pipelinePostProcessCounter = 0
        pipelineExceptionCounter = 0
        asyncPipelineExceptionCounter = 0
    }

    @Test
    fun `should process command with pipeline`() {
        val bus: CommandBus = CommandBusBuilder(MyCommand::class.java).build()
        bus.executeCommand(MyCommand())

        assertTrue { pipelinePreProcessCounter == 1 }
        assertTrue { pipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process command with async pipeline`() {
        val bus: CommandBus = CommandBusBuilder(MyAsyncCommand::class.java).build()
        runBlocking {
            bus.executeCommandAsync(MyAsyncCommand())

        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process exception in handler`() {
        val bus: CommandBus = CommandBusBuilder(MyBrokenCommand::class.java).build()
        val act = { bus.executeCommand(MyBrokenCommand()) }

        assertThrows<Exception> { act() }
        assertTrue { pipelineExceptionCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val bus: CommandBus = CommandBusBuilder(MyBrokenCommand::class.java).build()
        val act = suspend { bus.executeCommandAsync(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { asyncPipelineExceptionCounter == 1 }
    }
}

class MyBrokenCommand : Command

class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
    override fun handle(command: MyBrokenCommand) {
        throw Exception()
    }
}

class MyBrokenAsyncHandler : AsyncCommandHandler<MyBrokenCommand> {
    override suspend fun handleAsync(command: MyBrokenCommand) {
        throw Exception()
    }

}

class MyPipelineBehavior : PipelineBehavior {
    override fun <TRequest> preProcess(request: TRequest) {
        pipelinePreProcessCounter++
    }

    override fun <TRequest> postProcess(request: TRequest) {
        pipelinePostProcessCounter++
    }

    override fun <TRequest, TException : Exception> handleExceptionProcess(request: TRequest, exception: TException) {
        pipelineExceptionCounter++
    }
}

class MyAsyncPipelineBehavior : AsyncPipelineBehavior {
    override suspend fun <TRequest> preProcess(request: TRequest) {
        delay(500)
        asyncPipelinePreProcessCounter++
    }

    override suspend fun <TRequest> postProcess(request: TRequest) {
        delay(500)
        asyncPipelinePostProcessCounter++
    }

    override suspend fun <TRequest, TException : Exception> handleException(request: TRequest, exception: TException) {
        delay(500)
        asyncPipelineExceptionCounter++
    }
}