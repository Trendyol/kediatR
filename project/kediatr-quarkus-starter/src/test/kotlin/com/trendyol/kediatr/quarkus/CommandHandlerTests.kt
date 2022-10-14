package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Mediator
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        assertEquals("handler could not be found for com.trendyol.kediatr.quarkus.NonExistCommand", exception.message)
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
