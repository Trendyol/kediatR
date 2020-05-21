package com.trendyol

import com.trendyol.kediatr.AsyncPipelineBehavior
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.PipelineBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

var asyncPipelinePreProcessCounter = 0
var asyncPipelinePostProcessCounter = 0
var pipelinePreProcessCounter = 0
var pipelinePostProcessCounter = 0

class PipelineBehaviorTest {

    init {
        asyncPipelinePreProcessCounter = 0
        asyncPipelinePostProcessCounter = 0
        pipelinePreProcessCounter = 0
        pipelinePostProcessCounter = 0
    }

    @Test
    fun `should process command with pipeline`() {
        val bus: CommandBus = CommandBusBuilder(MyCommand::class.java).build()
        bus.executeCommand(MyCommand())

        assertTrue { pipelinePreProcessCounter == 1 }
        assertTrue { pipelinePostProcessCounter == 1 }
    }

    @Test
    fun `should process command with async pipeline`() {
        val bus: CommandBus = CommandBusBuilder(MyAsyncCommand::class.java).build()
        runBlocking {
            bus.executeCommandAsync(MyAsyncCommand())

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