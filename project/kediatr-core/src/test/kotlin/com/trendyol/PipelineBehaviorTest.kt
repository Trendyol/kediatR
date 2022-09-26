package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
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

    private class MyCommand : Command

    private class MyCommandHandler() : CommandHandler<MyCommand> {
        override fun handle(command: MyCommand) {
        }
    }

    private class MyAsyncCommand : Command

    private class AsyncMyCommandHandler : AsyncCommandHandler<MyAsyncCommand> {
        override suspend fun handleAsync(command: MyAsyncCommand) {
            delay(500)
        }
    }

    @Test
    fun `should process command with pipeline`() {
        val handler = MyCommandHandler()
        val pipeline = MyPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> =
            hashMapOf(Pair(MyCommandHandler::class.java, handler), Pair(MyPipelineBehavior::class.java, pipeline))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommand(MyCommand())

        assertTrue { pipelinePreProcessCounter == 1 }
        assertTrue { pipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process command with async pipeline`() {
        val handler = AsyncMyCommandHandler()
        val pipeline = MyAsyncPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(AsyncMyCommandHandler::class.java, handler),
            Pair(MyAsyncPipelineBehavior::class.java, pipeline)
        )
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        runBlocking {
            bus.executeCommandAsync(MyAsyncCommand())
        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process exception in handler`() {
        val handler = MyBrokenHandler()
        val pipeline = MyPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> =
            hashMapOf(Pair(MyBrokenHandler::class.java, handler), Pair(MyPipelineBehavior::class.java, pipeline))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val act = { bus.executeCommand(MyBrokenCommand()) }

        assertThrows<Exception> { act() }
        assertTrue { pipelineExceptionCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val handler = MyBrokenAsyncHandler()
        val pipeline = MyAsyncPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(MyBrokenAsyncHandler::class.java, handler),
            Pair(MyAsyncPipelineBehavior::class.java, pipeline)
        )
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val act = suspend { bus.executeCommandAsync(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { asyncPipelineExceptionCounter == 1 }
    }

    @Test
    fun `should process command with inherited pipeline`() = runBlocking {
        val handler = MyAsyncCommandHandler()
        val pipeline = InheritedPipelineBehaviour()
        val handlers: HashMap<Class<*>, Any> =
            hashMapOf(
                Pair(MyAsyncCommandHandler::class.java, handler),
                Pair(InheritedPipelineBehaviour::class.java, pipeline)
            )
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(AsyncCommand())

        assertEquals(1, asyncPipelinePreProcessCounter)
        assertEquals(1, asyncPipelinePostProcessCounter)
    }
}

private abstract class MyBasePipelineBehaviour : AsyncPipelineBehavior

private class InheritedPipelineBehaviour : MyBasePipelineBehaviour() {
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

private class AsyncCommand : Command
private class MyAsyncCommandHandler : AsyncCommandHandler<AsyncCommand> {
    override suspend fun handleAsync(command: AsyncCommand) {
    }
}

private class MyBrokenCommand : Command

private class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
    override fun handle(command: MyBrokenCommand) {
        throw Exception()
    }
}

private class MyBrokenAsyncHandler : AsyncCommandHandler<MyBrokenCommand> {
    override suspend fun handleAsync(command: MyBrokenCommand) {
        throw Exception()
    }
}

private class MyPipelineBehavior : PipelineBehavior {
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

private class MyAsyncPipelineBehavior : AsyncPipelineBehavior {
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
