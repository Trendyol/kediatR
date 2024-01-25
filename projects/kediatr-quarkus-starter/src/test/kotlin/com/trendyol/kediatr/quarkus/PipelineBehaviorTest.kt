package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.PipelineBehavior
import com.trendyol.kediatr.RequestHandlerDelegate
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

var exceptionPipelineBehaviorHandleCounter = 0
var exceptionPipelineBehaviorHandleCatchCounter = 0
var loggingPipelineBehaviorHandleBeforeNextCounter = 0
var loggingPipelineBehaviorHandleAfterNextCounter = 0
var commandTestCounter = 0

@QuarkusTest
class PipelineBehaviorTest {
    init {
        exceptionPipelineBehaviorHandleCounter = 0
        exceptionPipelineBehaviorHandleCatchCounter = 0
        loggingPipelineBehaviorHandleBeforeNextCounter = 0
        loggingPipelineBehaviorHandleAfterNextCounter = 0
        commandTestCounter = 0
    }

    @Inject
    lateinit var mediator: Mediator

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            mediator.send(MyPipelineCommand())
        }

        assertTrue { commandTestCounter == 1 }
        assertTrue { exceptionPipelineBehaviorHandleCatchCounter == 0 }
        assertTrue { exceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleAfterNextCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { mediator.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }

        assertTrue { commandTestCounter == 0 }
        assertTrue { exceptionPipelineBehaviorHandleCatchCounter == 1 }
        assertTrue { exceptionPipelineBehaviorHandleCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleBeforeNextCounter == 1 }
        assertTrue { loggingPipelineBehaviorHandleAfterNextCounter == 0 }
    }
}

class MyBrokenCommand : Command

class MyPipelineCommand : Command

@ApplicationScoped
@Startup
class MyPipelineCommandHandler(
    val mediator: Mediator
) : CommandHandler<MyPipelineCommand> {
    override suspend fun handle(command: MyPipelineCommand) {
        commandTestCounter++
    }
}

@ApplicationScoped
@Startup
class MyBrokenHandler(
    private val mediator: Mediator
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
        next: RequestHandlerDelegate<TRequest, TResponse>
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

@ApplicationScoped
@Startup
private class LoggingPipelineBehavior : PipelineBehavior {
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: RequestHandlerDelegate<TRequest, TResponse>
    ): TResponse {
        loggingPipelineBehaviorHandleBeforeNextCounter++
        val result = next(request)
        loggingPipelineBehaviorHandleAfterNextCounter++
        return result
    }
}
