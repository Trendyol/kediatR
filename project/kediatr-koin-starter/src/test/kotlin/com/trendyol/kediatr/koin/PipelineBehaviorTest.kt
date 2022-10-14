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
                single { MyPipelineBehavior(get()) } bind MyPipelineBehavior::class
                single { MyCommandHandler(get()) } bind CommandHandler::class
                single { MyAsyncCommandRHandler(get()) } bind CommandWithResultHandler::class
                single { MyFirstNotificationHandler(get()) } bind NotificationHandler::class
                single { TestQueryHandler(get()) } bind QueryHandler::class
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

    private val commandBus by inject<Mediator>()

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.send(MyCommand())
        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { commandBus.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { asyncPipelineExceptionCounter == 1 }
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

class MyPipelineBehavior(
    private val commandBus: Mediator,
) : PipelineBehavior {
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
