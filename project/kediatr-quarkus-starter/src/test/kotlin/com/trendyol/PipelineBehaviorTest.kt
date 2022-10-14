package com.trendyol

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
    lateinit var commandBus: Mediator

    @Test
    fun `should process command with async pipeline`() {
        runBlocking {
            commandBus.send(MyPipelineCommand())
        }

        assertTrue { asyncPipelinePreProcessCounter == 1 }
        assertTrue { asyncPipelinePostProcessCounter == 1 }
        assertTrue { commandAsyncTestCounter == 1 }
    }

    @Test
    fun `should process exception in async handler`() {
        val act = suspend { commandBus.send(MyBrokenCommand()) }

        assertThrows<Exception> { runBlocking { act() } }
        assertTrue { asyncPipelineExceptionCounter == 1 }
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
