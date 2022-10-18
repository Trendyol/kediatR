package com.trendyol.kediatr.spring

import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.PipelineBehavior
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.RequestHandlerDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

var exceptionPipelineBehaviorHandleCounter = 0
var exceptionPipelineBehaviorHandleCatchCounter = 0
var loggingPipelineBehaviorHandleBeforeNextCounter = 0
var loggingPipelineBehaviorHandleAfterNextCounter = 0

@SpringBootTest(
    classes = [KediatrConfiguration::class, MyCommandHandler::class, ExceptionPipelineBehavior::class, LoggingPipelineBehavior::class]
)
class PipelineBehaviorTest {

    init {
        exceptionPipelineBehaviorHandleCounter = 0
        exceptionPipelineBehaviorHandleCatchCounter = 0
        loggingPipelineBehaviorHandleBeforeNextCounter = 0
        loggingPipelineBehaviorHandleAfterNextCounter = 0
    }

    @Autowired
    lateinit var commandBus: Mediator

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.send(MyCommand())
        }

        assertTrue { exceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { exceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleAfterNextCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { commandBus.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }

        assertTrue { exceptionPipelineBehaviorHandleCatchCounter == 1 }
        assertTrue { exceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleAfterNextCounter == 0 }
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
