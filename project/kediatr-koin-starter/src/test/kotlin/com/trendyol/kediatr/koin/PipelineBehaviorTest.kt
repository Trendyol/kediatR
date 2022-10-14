package com.trendyol.kediatr.koin

import com.trendyol.kediatr.AsyncCommandHandler
import com.trendyol.kediatr.AsyncCommandWithResultHandler
import com.trendyol.kediatr.AsyncNotificationHandler
import com.trendyol.kediatr.AsyncPipelineBehavior
import com.trendyol.kediatr.AsyncQueryHandler
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandBus
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

var asyncPipelinePreProcessCounter = 0
var asyncPipelinePostProcessCounter = 0
var pipelinePreProcessCounter = 0
var pipelinePostProcessCounter = 0
var pipelineExceptionCounter = 0
var asyncPipelineExceptionCounter = 0

class PipelineBehaviorTest : KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { MyAsyncPipelineBehavior(get()) } bind MyAsyncPipelineBehavior::class
                single { MyAsyncCommandHandler(get()) } bind AsyncCommandHandler::class
                single { MyAsyncCommandRHandler(get()) } bind AsyncCommandWithResultHandler::class
                single { MyFirstAsyncNotificationHandler(get()) } bind AsyncNotificationHandler::class
                single { AsyncTestQueryHandler(get()) } bind AsyncQueryHandler::class
            }
        )
    }

    init {
        asyncPipelinePreProcessCounter = 0
        asyncPipelinePostProcessCounter = 0
        pipelinePreProcessCounter = 0
        pipelinePostProcessCounter = 0
        pipelineExceptionCounter = 0
        asyncPipelineExceptionCounter = 0
    }

    private val commandBus by inject<CommandBus>()

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.executeCommandAsync(MyCommand())
        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { commandBus.executeCommandAsync(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { asyncPipelineExceptionCounter == 1 }
    }
}

class MyBrokenCommand : Command

class MyBrokenAsyncHandler(
    private val commandBus: CommandBus,
) : AsyncCommandHandler<MyBrokenCommand> {
    override suspend fun handleAsync(command: MyBrokenCommand) {
        delay(500)
        throw Exception()
    }
}

class MyAsyncPipelineBehavior(
    private val commandBus: CommandBus,
) : AsyncPipelineBehavior {
    override suspend fun <TRequest> preProcess(request: TRequest) {
        delay(500)
        asyncPipelinePreProcessCounter++
    }

    override suspend fun <TRequest> postProcess(request: TRequest) {
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
