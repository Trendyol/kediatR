package com.trendyol.kediatr.koin

import com.trendyol.kediatr.*
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

class PipelineBehaviorTest: KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { MyPipelineBehavior(get()) } bind PipelineBehavior::class
                single { MyAsyncPipelineBehavior(get()) } bind MyAsyncPipelineBehavior::class
                single { MyCommandHandler(get()) } bind CommandHandler::class
                single { MyAsyncCommandHandler(get()) } bind AsyncCommandHandler::class
                single { MyCommandRHandler(get()) } bind CommandWithResultHandler::class
                single { MyAsyncCommandRHandler(get()) } bind AsyncCommandWithResultHandler::class
                single { MyFirstNotificationHandler(get()) } bind NotificationHandler::class
                single { MyFirstAsyncNotificationHandler(get()) } bind AsyncNotificationHandler::class
                single { MySecondNotificationHandler(get()) } bind NotificationHandler::class
                single { TestQueryHandler(get()) } bind QueryHandler::class
                single { AsyncTestQueryHandler(get()) } bind AsyncQueryHandler::class
            },
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
    fun `should process command with pipeline`() {
        commandBus.executeCommand(MyCommand())

        assertTrue { pipelinePreProcessCounter == 1 }
        assertTrue { pipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.executeCommandAsync(MyCommand())

        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process exception in handler`() {
        val act = { commandBus.executeCommand(MyBrokenCommand()) }

        assertThrows<Exception> { act() }
        assertTrue { pipelineExceptionCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { commandBus.executeCommandAsync(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { asyncPipelineExceptionCounter == 1 }
    }
}

class MyBrokenCommand : Command

class MyBrokenHandler(
    private val commandBus: CommandBus
) : CommandHandler<MyBrokenCommand> {
    override fun handle(command: MyBrokenCommand) {
        throw Exception()
    }
}

class MyBrokenAsyncHandler(
    private val commandBus: CommandBus
) : AsyncCommandHandler<MyBrokenCommand> {
    override suspend fun handleAsync(command: MyBrokenCommand) {
        delay(500)
        throw Exception()
    }
}

class MyPipelineBehavior(
    private val commandBus: CommandBus
) : PipelineBehavior{
    override fun <TRequest> preProcess(request: TRequest) {
        pipelinePreProcessCounter++
    }

    override fun <TRequest> postProcess(request: TRequest) {
        pipelinePostProcessCounter++
    }

    override fun <TRequest, TException : Exception> handleExceptionProcess(request: TRequest, exception: TException) {
        pipelineExceptionCounter++
    }
}

class MyAsyncPipelineBehavior(
    private val commandBus: CommandBus
) : AsyncPipelineBehavior {
    override suspend fun <TRequest> preProcess(request: TRequest) {
        delay(500)
        asyncPipelinePreProcessCounter++
    }

    override suspend fun <TRequest> postProcess(request: TRequest) {
        delay(500)
        asyncPipelinePostProcessCounter++
    }

    override suspend fun <TRequest, TException : Exception> handleException(request: TRequest, exception: TException) {
        delay(500)
        asyncPipelineExceptionCounter++
    }
}