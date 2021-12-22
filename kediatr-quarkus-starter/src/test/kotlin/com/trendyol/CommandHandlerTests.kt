package com.trendyol

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@QuarkusTest
class CommandHandlerTests {
    @Inject
    lateinit var commandBus: CommandBus

    @Test
    fun `commandHandler should be fired`() = runBlocking {
        commandBus.executeCommand(MyCommand())
        assertTrue {
            springTestCounter == 1
        }
    }

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        commandBus.executeCommandAsync(MyCommand())
        assertTrue {
            springAsyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.executeCommandAsync(NonExistCommand())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommand")
    }

    @Test
    fun `should throw exception if given command does not have handler bean`() {

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            commandBus.executeCommand(NonExistCommand())
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
    val commandBus: CommandBus
) : CommandHandler<MyCommand> {
    override fun handle(command: MyCommand) {
        springTestCounter++
    }
}

@ApplicationScoped
@Startup
class MyAsyncCommandHandler(
    val commandBus: CommandBus
) : AsyncCommandHandler<MyCommand> {
    override suspend fun handleAsync(command: MyCommand) {
        delay(500)
        springAsyncTestCounter++
    }
}
