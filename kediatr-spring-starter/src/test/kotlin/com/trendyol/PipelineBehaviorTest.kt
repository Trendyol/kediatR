package com.trendyol

import com.trendyol.kediatr.AsyncPipelineBehavior
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.PipelineBehavior
import com.trendyol.kediatr.spring.KediatrConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertTrue

var asyncPipelinePreProcessCounter = 0
var asyncPipelinePostProcessCounter = 0
var pipelinePreProcessCounter = 0
var pipelinePostProcessCounter = 0

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [KediatrConfiguration::class, MyAsyncCommandHandler::class, MyCommandHandler::class, MyPipelineBehavior::class, MyAsyncPipelineBehavior::class])
class PipelineBehaviorTest {

    init {
        asyncPipelinePreProcessCounter = 0
        asyncPipelinePostProcessCounter = 0
        pipelinePreProcessCounter = 0
        pipelinePostProcessCounter = 0
    }

    @Autowired
    lateinit var commandBus: CommandBus


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
}

class MyPipelineBehavior : PipelineBehavior {
    override fun <TRequest> preProcess(request: TRequest) {
        pipelinePreProcessCounter++
    }

    override fun <TRequest> postProcess(request: TRequest) {
        pipelinePostProcessCounter++
    }
}

class MyAsyncPipelineBehavior : AsyncPipelineBehavior {
    override suspend fun <TRequest> preProcess(request: TRequest) {
        delay(500)
        asyncPipelinePreProcessCounter++
    }

    override suspend fun <TRequest> postProcess(request: TRequest) {
        delay(500)
        asyncPipelinePostProcessCounter++
    }
}