package com.trendyol.kediatr.spring

import com.trendyol.kediatr.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.*

private var springTestCounter = 0
private var springAsyncTestCounter = 0

@SpringBootTest(classes = [KediatRAutoConfiguration::class, MyAsyncCommandRHandler::class])
class CommandWithResultHandlerTest {
    init {
        springTestCounter = 0
        springAsyncTestCounter = 0
    }

    @Autowired
    lateinit var mediator: Mediator

    @Test
    fun `async commandHandler should be fired`() =
        runBlocking {
            mediator.send(MyCommandR())

            assertTrue {
                springAsyncTestCounter == 1
            }
        }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception =
            assertFailsWith(HandlerNotFoundException::class) {
                runBlocking {
                    mediator.send(NonExistCommandR())
                }
            }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.kediatr.spring.NonExistCommandR")
    }
}

class Result

class NonExistCommandR : CommandWithResult<Result>

class MyCommandR : CommandWithResult<Result>

class MyAsyncCommandRHandler : CommandWithResultHandler<MyCommandR, Result> {
    override suspend fun handle(command: MyCommandR): Result {
        delay(500)
        springAsyncTestCounter++

        return Result()
    }
}
