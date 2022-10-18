package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

var asyncExceptionPipelineBehaviorHandleCounter = 0
var asyncExceptionPipelineBehaviorHandleCatchCounter = 0
var asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
var asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0
var commandAsyncTestCounter = 0

@QuarkusTest
class PipelineBehaviorTest {

    init {
        asyncExceptionPipelineBehaviorHandleCounter = 0
        asyncExceptionPipelineBehaviorHandleCatchCounter = 0
        asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
        asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0
        commandAsyncTestCounter = 0
    }

    @Inject
    lateinit var commandBus: Mediator

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.send(MyPipelineCommand())
        }

        assertTrue { commandAsyncTestCounter == 1 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleAfterNextCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { commandBus.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }

        assertTrue { commandAsyncTestCounter == 0 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCatchCounter == 1 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleAfterNextCounter == 0 }
    }
}

class MyBrokenCommand : Command

class MyPipelineCommand : Command

@ApplicationScoped
@Startup
class MyPipelineCommandHandler(
    val commandBus: Mediator,
) : CommandHandler<MyPipelineCommand> {
    override suspend fun handle(command: MyPipelineCommand) {
        commandAsyncTestCounter++
    }
}

@ApplicationScoped
@Startup
class MyBrokenHandler(
    private val commandBus: Mediator,
) : CommandHandler<MyBrokenCommand> {
    override suspend fun handle(command: MyBrokenCommand) {
        delay(500)
        throw Exception()
    }
}

@ApplicationScoped
@Startup
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

@ApplicationScoped
@Startup
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
