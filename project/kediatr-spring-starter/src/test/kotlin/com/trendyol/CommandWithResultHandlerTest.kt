package com.trendyol

import com.trendyol.kediatr.*
import com.trendyol.kediatr.spring.KediatrConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.*

private var springTestCounter = 0
private var springAsyncTestCounter = 0

@SpringBootTest(classes = [KediatrConfiguration::class, MyAsyncCommandRHandler::class])
class CommandWithResultHandlerTest {

    init {
        springTestCounter = 0
        springAsyncTestCounter = 0
    }

    @Autowired
    lateinit var commandBus: CommandBus

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        commandBus.executeCommandAsync(MyCommandR())

        assertTrue {
            springAsyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.executeCommandAsync(NonExistCommandR())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommandR")
    }
}

class Result

class NonExistCommandR : CommandWithResult<Result>
class MyCommandR : CommandWithResult<Result>

class MyAsyncCommandRHandler : AsyncCommandWithResultHandler<MyCommandR, Result> {
    override suspend fun handleAsync(command: MyCommandR): Result {
        delay(500)
        springAsyncTestCounter++

        return Result()
    }
}
