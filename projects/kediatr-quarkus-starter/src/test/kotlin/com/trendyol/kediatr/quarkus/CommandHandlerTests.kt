package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Mediator
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
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
    lateinit var mediator: Mediator

    @Test
    fun `async commandHandler should be fired`() =
        runBlocking {
            mediator.send(MyCommand())
            assertTrue {
                springAsyncTestCounter == 1
            }
        }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception =
            assertFailsWith(HandlerNotFoundException::class) {
                runBlocking {
                    mediator.send(NonExistCommand())
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
    val mediator: Mediator
) : CommandHandler<MyCommand> {
    override suspend fun handle(command: MyCommand) {
        delay(500)
        springAsyncTestCounter++
    }
}
