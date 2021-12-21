package com.trendyol

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.test.assertTrue

var asyncPipelinePreProcessCounter = 0
var asyncPipelinePostProcessCounter = 0
var pipelinePreProcessCounter = 0
var pipelinePostProcessCounter = 0
var pipelineExceptionCounter = 0
var asyncPipelineExceptionCounter = 0
var commandTestCounter = 0
var commandAsyncTestCounter = 0

@QuarkusTest
class PipelineBehaviorTest {

    init {
        asyncPipelinePreProcessCounter = 0
        asyncPipelinePostProcessCounter = 0
        pipelinePreProcessCounter = 0
        pipelinePostProcessCounter = 0
        pipelineExceptionCounter = 0
        asyncPipelineExceptionCounter = 0
        commandTestCounter = 0
        commandAsyncTestCounter = 0
    }

    @Inject
    lateinit var commandBus: CommandBus

    @Test
    fun `should process command with pipeline`() {
        commandBus.executeCommand(MyPipelineCommand())

        assertTrue { pipelinePreProcessCounter == 1 }
        assertTrue { pipelinePostProcessCounter == 1 }
        assertTrue { commandTestCounter == 1 }
    }

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.executeCommandAsync(MyPipelineCommand())

        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
        assertTrue { commandAsyncTestCounter == 1 }
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

class MyPipelineCommand : Command

@ApplicationScoped
@Startup
class MyPipelineCommandHandler(
    val commandBus: CommandBus
) : CommandHandler<MyPipelineCommand> {
    override fun handle(command: MyPipelineCommand) {
        commandTestCounter++
    }
}

@ApplicationScoped
@Startup
class MyPipelineCommandAsyncHandler(
    val commandBus: CommandBus
) : AsyncCommandHandler<MyPipelineCommand> {
    override suspend fun handleAsync(command: MyPipelineCommand) {
        commandAsyncTestCounter++
    }

}

@ApplicationScoped
@Startup
class MyBrokenHandler(
    private val commandBus: CommandBus
) : CommandHandler<MyBrokenCommand> {
    override fun handle(command: MyBrokenCommand) {
        throw Exception()
    }
}

@ApplicationScoped
@Startup
class MyBrokenAsyncHandler(
    private val commandBus: CommandBus
) : AsyncCommandHandler<MyBrokenCommand> {
    override suspend fun handleAsync(command: MyBrokenCommand) {
        delay(500)
        throw Exception()
    }
}

@ApplicationScoped
@Startup
class MyPipelineBehavior(
    private val commandBus: CommandBus
) : PipelineBehavior {
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

@ApplicationScoped
@Startup
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