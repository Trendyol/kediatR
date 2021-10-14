package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private var counter = 0
private var asyncTestCounter = 0

class CommandHandlerTest {

    init {
        counter = 0
        asyncTestCounter = 0
    }

    @Test
    fun `commandHandler should be fired`() {
        val handler = MyCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyCommandHandler::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommand(MyCommand())

        assertTrue {
            counter == 1
        }
    }

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        val handler = AsyncMyCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AsyncMyCommandHandler::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyAsyncCommand())

        assertTrue {
            asyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command has not been registered before`() {
        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                bus.executeCommandAsync(NonExistCommand())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommand")
    }

    @Test
    fun `should throw exception if given command has not been registered before`() {
        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            bus.executeCommand(NonExistCommand())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommand")
    }
}

class NonExistCommand : Command

class MyCommand : Command

class MyCommandHandler(
) : CommandHandler<MyCommand> {
    override fun handle(command: MyCommand) {
        counter++
    }
}

class MyAsyncCommand : Command

class AsyncMyCommandHandler : AsyncCommandHandler<MyAsyncCommand> {
    override suspend fun handleAsync(command: MyAsyncCommand) {
        delay(500)
        asyncTestCounter++
    }
}