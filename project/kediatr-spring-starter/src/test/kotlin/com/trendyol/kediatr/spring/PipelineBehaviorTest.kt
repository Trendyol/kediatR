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

var asyncPipelinePreProcessCounter = 0
var asyncPipelinePostProcessCounter = 0
var pipelinePreProcessCounter = 0
var pipelinePostProcessCounter = 0
var pipelineExceptionCounter = 0
var asyncPipelineExceptionCounter = 0

@SpringBootTest(
    classes = [KediatrConfiguration::class, MyCommandHandler::class, MyPipelineBehavior::class]
)
class PipelineBehaviorTest {

    init {
        asyncPipelinePreProcessCounter = 0
        asyncPipelinePostProcessCounter = 0
        pipelinePreProcessCounter = 0
        pipelinePostProcessCounter = 0
        pipelineExceptionCounter = 0
        asyncPipelineExceptionCounter = 0
    }

    @Autowired
    lateinit var commandBus: Mediator

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

class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
    override suspend fun handle(command: MyBrokenCommand) {
        delay(500)
        throw Exception()
    }
}

class MyPipelineBehavior : PipelineBehavior {
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
