package com.trendyol

import com.trendyol.kediatr.*
import com.trendyol.kediatr.spring.HandlerBeanNotFoundException
import com.trendyol.kediatr.spring.KediatrConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


private var springTestCounter = 0
private var springAsyncTestCounter = 0

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [KediatrConfiguration::class, MyAsyncCommandRHandler::class, MyCommandRHandler::class])
class CommandWithResultHandlerTest {

    init {
        springTestCounter = 0
        springAsyncTestCounter = 0
    }

    @Autowired
    lateinit var commandBus: CommandBus

    @Test
    fun `commandHandler should be fired`() {
        commandBus.executeCommand(MyCommandR())
        assertTrue {
            springTestCounter == 1
        }
    }

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        commandBus.executeCommandAsync(MyCommandR())

        assertTrue {
            springAsyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {

        val exception = assertFailsWith(HandlerBeanNotFoundException::class) {
            runBlocking {
                commandBus.executeCommandAsync(NonExistCommandR())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommandR")
    }

    @Test
    fun `should throw exception if given command does not have handler bean`() {

        val exception = assertFailsWith(HandlerBeanNotFoundException::class) {
            commandBus.executeCommand(NonExistCommandR())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommandR")
    }
}

private class Result

private class NonExistCommandR : CommandWithResult<Result>
private class MyCommandR : CommandWithResult<Result>

private class MyCommandRHandler : CommandWithResultHandler<MyCommandR, Result> {
    override fun handle(command: MyCommandR): Result {
        springTestCounter++

        return Result()
    }
}

private class MyAsyncCommandRHandler : AsyncCommandWithResultHandler<MyCommandR, Result> {
    override suspend fun handleAsync(command: MyCommandR): Result {
        delay(500)
        springAsyncTestCounter++

        return Result()
    }
}


