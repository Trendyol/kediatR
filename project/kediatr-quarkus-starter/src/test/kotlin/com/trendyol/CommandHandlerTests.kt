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

@QuarkusTest
class CommandHandlerTests {
    @Inject
    lateinit var commandBus: Mediator

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        commandBus.send(MyCommand())
        assertTrue {
            springAsyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.send(NonExistCommand())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommand")
    }
}

private var springTestCounter = 0
private var springAsyncTestCounter = 0

class NonExistCommand : Command
class MyCommand : Command

@ApplicationScoped
@Startup
class MyCommandHandler(
    val commandBus: Mediator,
) : CommandHandler<MyCommand> {
    override suspend fun handle(command: MyCommand) {
        delay(500)
        springAsyncTestCounter++
    }
}
