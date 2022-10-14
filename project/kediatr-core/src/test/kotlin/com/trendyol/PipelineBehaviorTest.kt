package com.trendyol

import com.trendyol.MyResult.MyResultClass
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.CommandWithResult
import com.trendyol.kediatr.CommandWithResultHandler
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.MediatorBuilder
import com.trendyol.kediatr.PipelineBehavior
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
var postProcessWithResult = false

class PipelineBehaviorTest {

    init {
        asyncPipelinePreProcessCounter = 0
        asyncPipelinePostProcessCounter = 0
        pipelinePreProcessCounter = 0
        pipelinePostProcessCounter = 0
        pipelineExceptionCounter = 0
        asyncPipelineExceptionCounter = 0
        postProcessWithResult = false
    }

    private class MyCommand : Command

    private class MyCommandHandler : CommandHandler<MyCommand> {
        override suspend fun handle(command: MyCommand) {
            delay(500)
        }
    }

    @Test
    fun `should process command with async pipeline`() {
        val handler = MyCommandHandler()
        val pipeline = MyPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(MyCommandHandler::class.java, handler),
            Pair(MyPipelineBehavior::class.java, pipeline)
        )
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()

        runBlocking {
            bus.send(MyCommand())
        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val handler = MyBrokenHandler()
        val pipeline = MyPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(MyBrokenHandler::class.java, handler),
            Pair(MyPipelineBehavior::class.java, pipeline)
        )
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()
        val act = suspend { bus.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { asyncPipelineExceptionCounter == 1 }
    }

    @Test
    fun `should process command with inherited pipeline`() = runBlocking {
        val handler = MyCommandHandler()
        val pipeline = InheritedPipelineBehaviour()
        val handlers: HashMap<Class<*>, Any> =
            hashMapOf(
                Pair(MyCommandHandler::class.java, handler),
                Pair(InheritedPipelineBehaviour::class.java, pipeline)
            )
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()
        bus.send(MyCommand())

        assertEquals(1, asyncPipelinePreProcessCounter)
        assertEquals(1, asyncPipelinePostProcessCounter)
    }

    @Test
    fun `should process result in post process pipeline behavior`() = runBlocking {
        val handler = MyCommandWithResultHandler()
        val pipeline = MyResponsePipelineBehavior()
        val handlers: HashMap<Class<*>, Any> =
            hashMapOf(Pair(MyCommandWithResultHandler::class.java, handler), Pair(MyPipelineBehavior::class.java, pipeline))
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()
        bus.send(MyCommandWithResult())

        assertTrue { postProcessWithResult }
    }
}

private abstract class MyBasePipelineBehaviour : PipelineBehavior

private class InheritedPipelineBehaviour : MyBasePipelineBehaviour() {
    override suspend fun <TRequest> preProcess(request: TRequest) {
        delay(500)
        asyncPipelinePreProcessCounter++
    }

    override suspend fun <TRequest, TResponse> postProcess(request: TRequest, response: TResponse) {
        delay(500)
        asyncPipelinePostProcessCounter++
    }

    override suspend fun <TRequest, TException : Exception> handleException(
        request: TRequest,
        exception: TException,
    ) {
        delay(500)
        asyncPipelineExceptionCounter++
    }
}

private class MyCommand : Command
private class MyBrokenCommand : Command

private class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
    override suspend fun handle(command: MyBrokenCommand) {
        throw Exception()
    }
}

private class MyPipelineBehavior : PipelineBehavior {
    override suspend fun <TRequest> preProcess(request: TRequest) {
        delay(500)
        asyncPipelinePreProcessCounter++
    }

    override suspend fun <TRequest, TResponse> postProcess(request: TRequest, response: TResponse) {
        delay(500)
        asyncPipelinePostProcessCounter++
    }

    override suspend fun <TRequest, TException : Exception> handleException(
        request: TRequest,
        exception: TException,
    ) {
        delay(500)
        asyncPipelineExceptionCounter++
    }
}

sealed class MyResult {
    object MyResultClass : MyResult()
}
private class MyCommandWithResult : CommandWithResult<MyResultClass>

private class MyCommandWithResultHandler() : CommandWithResultHandler<MyCommandWithResult, MyResultClass> {
    override suspend fun handle(command: MyCommandWithResult): MyResultClass {
        return MyResultClass
    }
}

private class MyResponsePipelineBehavior : PipelineBehavior {
    override suspend fun <TRequest> preProcess(request: TRequest) {
    }

    override suspend fun <TRequest, TResponse> postProcess(request: TRequest, response: TResponse) {
        if (response is MyResultClass) {
            postProcessWithResult = true
        }
    }

    override suspend fun <TRequest, TException : Exception> handleException(
        request: TRequest,
        exception: TException,
    ) {
    }
}
