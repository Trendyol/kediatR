package com.trendyol.kediatr

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

var asyncExceptionPipelineBehaviorHandleCounter = 0
var asyncExceptionPipelineBehaviorHandleCatchCounter = 0
var asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
var asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0
var asyncInheritedPipelineBehaviourHandleCounter = 0
var commandAsyncTestCounter = 0

class PipelineBehaviorTest {

    init {
        asyncExceptionPipelineBehaviorHandleCounter = 0
        asyncExceptionPipelineBehaviorHandleCatchCounter = 0
        asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
        asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0
        asyncInheritedPipelineBehaviourHandleCounter = 0
        commandAsyncTestCounter = 0
    }

    private class MyCommand : Command

    private class MyCommandHandler : CommandHandler<MyCommand> {
        override suspend fun handle(command: MyCommand) {
            commandAsyncTestCounter++
            delay(500)
        }
    }

    @Test
    fun `should process command without async pipeline`() {
        val handler = MyCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(MyCommandHandler::class.java, handler)
        )

        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()

        runBlocking {
            bus.send(MyCommand())
        }

        assertTrue { commandAsyncTestCounter == 1 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCounter == 0 }
        assertTrue { asyncLoggingPipelineBehaviorHandleBeforeNextCounter == 0 }
        assertTrue { asyncLoggingPipelineBehaviorHandleAfterNextCounter == 0 }
    }

    @Test
    fun `should process command with async pipeline`() {
        val handler = MyCommandHandler()
        val exceptionPipeline = ExceptionPipelineBehavior()
        val loggingPipeline = LoggingPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(MyCommandHandler::class.java, handler),
            Pair(ExceptionPipelineBehavior::class.java, exceptionPipeline),
            Pair(LoggingPipelineBehavior::class.java, loggingPipeline)
        )

        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()

        runBlocking {
            bus.send(MyCommand())
        }

        assertTrue { commandAsyncTestCounter == 1 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleAfterNextCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val handler = MyBrokenHandler()
        val exceptionPipeline = ExceptionPipelineBehavior()
        val loggingPipeline = LoggingPipelineBehavior()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(MyBrokenHandler::class.java, handler),
            Pair(ExceptionPipelineBehavior::class.java, exceptionPipeline),
            Pair(LoggingPipelineBehavior::class.java, loggingPipeline)
        )
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()
        val act = suspend { bus.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { commandAsyncTestCounter == 0 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCatchCounter == 1 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleAfterNextCounter == 0 }
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

        assertEquals(1, asyncInheritedPipelineBehaviourHandleCounter)
    }
}

private abstract class MyBasePipelineBehaviour : PipelineBehavior

private class InheritedPipelineBehaviour : MyBasePipelineBehaviour() {
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: suspend (TRequest) -> TResponse,
    ): TResponse {
        asyncInheritedPipelineBehaviourHandleCounter++
        return next(request)
    }
}

private class MyCommand : Command
private class MyBrokenCommand : Command

private class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
    override suspend fun handle(command: MyBrokenCommand) {
        throw Exception()
    }
}

private class ExceptionPipelineBehavior : PipelineBehavior {
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: suspend (TRequest) -> TResponse,
    ): TResponse {
        try {
            asyncExceptionPipelineBehaviorHandleCounter++
            return next(request)
        } catch (ex: Exception) {
            asyncExceptionPipelineBehaviorHandleCatchCounter++
            throw ex
        }
    }
}

private class LoggingPipelineBehavior : PipelineBehavior {
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: suspend (TRequest) -> TResponse,
    ): TResponse {
        asyncLoggingPipelineBehaviorHandleBeforeNextCounter++
        val result = next(request)
        asyncLoggingPipelineBehaviorHandleAfterNextCounter++
        return result
    }
}
