package com.trendyol.kediatr.spring

import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.PipelineBehavior
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.Mediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

var asyncExceptionPipelineBehaviorHandleCounter = 0
var asyncExceptionPipelineBehaviorHandleCatchCounter = 0
var asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
var asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0

@SpringBootTest(
    classes = [KediatrConfiguration::class, MyCommandHandler::class, ExceptionPipelineBehavior::class, LoggingPipelineBehavior::class]
)
class PipelineBehaviorTest {

    init {
        asyncExceptionPipelineBehaviorHandleCounter = 0
        asyncExceptionPipelineBehaviorHandleCatchCounter = 0
        asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
        asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0
    }

    @Autowired
    lateinit var commandBus: Mediator

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.send(MyCommand())
        }

        assertTrue { asyncExceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleAfterNextCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { commandBus.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }

        assertTrue { asyncExceptionPipelineBehaviorHandleCatchCounter == 1 }
        assertTrue { asyncExceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { asyncLoggingPipelineBehaviorHandleAfterNextCounter == 0 }
    }
}

class MyBrokenCommand : Command

class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
    override suspend fun handle(command: MyBrokenCommand) {
        delay(500)
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
