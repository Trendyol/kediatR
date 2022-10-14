package com.trendyol

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.*

private var testCounter = 0
private var asyncTestCounter = 0

@QuarkusTest
class CommandWithResultHandlerTest {

    init {
        testCounter = 0
        asyncTestCounter = 0
    }

    @Inject
    lateinit var commandBus: CommandBus

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        commandBus.executeCommandAsync(MyCommandR())

        assertTrue {
            asyncTestCounter == 1
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

@ApplicationScoped
@Startup
class MyAsyncCommandRHandler(
    val commandBus: CommandBus,
) : AsyncCommandWithResultHandler<MyCommandR, Result> {
    override suspend fun handleAsync(command: MyCommandR): Result {
        delay(500)
        asyncTestCounter++

        return Result()
    }
}
