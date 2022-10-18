package com.trendyol.kediatr.koin

import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.CommandWithResultHandler
import com.trendyol.kediatr.NotificationHandler
import com.trendyol.kediatr.PipelineBehavior
import com.trendyol.kediatr.QueryHandler
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.Mediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.assertTrue

var asyncExceptionPipelineBehaviorHandleCounter = 0
var asyncExceptionPipelineBehaviorHandleCatchCounter = 0
var asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
var asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0

class PipelineBehaviorTest : KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { ExceptionPipelineBehavior() } bind ExceptionPipelineBehavior::class
                single { LoggingPipelineBehavior() } bind LoggingPipelineBehavior::class
                single { MyCommandHandler(get()) } bind CommandHandler::class
                single { MyAsyncCommandRHandler(get()) } bind CommandWithResultHandler::class
                single { MyFirstNotificationHandler(get()) } bind NotificationHandler::class
                single { TestQueryHandler(get()) } bind QueryHandler::class
            }
        )
    }

    init {
        asyncExceptionPipelineBehaviorHandleCounter = 0
        asyncExceptionPipelineBehaviorHandleCatchCounter = 0
        asyncLoggingPipelineBehaviorHandleBeforeNextCounter = 0
        asyncLoggingPipelineBehaviorHandleAfterNextCounter = 0
    }

    private val commandBus by inject<Mediator>()

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

class MyBrokenHandler(
    private val commandBus: Mediator,
) : CommandHandler<MyBrokenCommand> {
    override suspend fun handle(command: MyBrokenCommand) {
        delay(500)
        throw Exception()
    }
}

class ExceptionPipelineBehavior : PipelineBehavior {
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

class LoggingPipelineBehavior : PipelineBehavior {
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
