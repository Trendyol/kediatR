package com.trendyol.kediatr

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

var exceptionPipelineBehaviorHandleCounter = 0
var exceptionPipelineBehaviorHandleCatchCounter = 0
var loggingPipelineBehaviorHandleBeforeNextCounter = 0
var loggingPipelineBehaviorHandleAfterNextCounter = 0
var inheritedPipelineBehaviourHandleCounter = 0
var commandTestCounter = 0

class PipelineBehaviorTest {

    init {
        exceptionPipelineBehaviorHandleCounter = 0
        exceptionPipelineBehaviorHandleCatchCounter = 0
        loggingPipelineBehaviorHandleBeforeNextCounter = 0
        loggingPipelineBehaviorHandleAfterNextCounter = 0
        inheritedPipelineBehaviourHandleCounter = 0
        commandTestCounter = 0
    }

    private class MyCommand : Command

    private class MyCommandHandler : CommandHandler<MyCommand> {
        override suspend fun handle(command: MyCommand) {
            commandTestCounter++
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

        assertTrue { commandTestCounter == 1 }
        assertTrue { exceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { exceptionPipelineBehaviorHandleCounter == 0 }
        assertTrue { loggingPipelineBehaviorHandleBeforeNextCounter == 0 }
        assertTrue { loggingPipelineBehaviorHandleAfterNextCounter == 0 }
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

        assertTrue { commandTestCounter == 1 }
        assertTrue { exceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { exceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleAfterNextCounter == 1 }
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
        assertTrue { commandTestCounter == 0 }
        assertTrue { exceptionPipelineBehaviorHandleCatchCounter == 1 }
        assertTrue { exceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleAfterNextCounter == 0 }
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

        assertEquals(1, inheritedPipelineBehaviourHandleCounter)
    }
}

private abstract class MyBasePipelineBehaviour : PipelineBehavior

private class InheritedPipelineBehaviour : MyBasePipelineBehaviour() {
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: RequestHandlerDelegate<TRequest, TResponse>,
    ): TResponse {
        inheritedPipelineBehaviourHandleCounter++
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
        next: RequestHandlerDelegate<TRequest, TResponse>,
    ): TResponse {
        try {
            exceptionPipelineBehaviorHandleCounter++
            return next(request)
        } catch (ex: Exception) {
            exceptionPipelineBehaviorHandleCatchCounter++
            throw ex
        }
    }
}

private class LoggingPipelineBehavior : PipelineBehavior {
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: RequestHandlerDelegate<TRequest, TResponse>,
    ): TResponse {
        loggingPipelineBehaviorHandleBeforeNextCounter++
        val result = next(request)
        loggingPipelineBehaviorHandleAfterNextCounter++
        return result
    }
}
