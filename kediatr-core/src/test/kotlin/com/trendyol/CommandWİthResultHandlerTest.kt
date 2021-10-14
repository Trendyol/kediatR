package com.trendyol

import com.trendyol.kediatr.*
import com.trendyol.kediatr.PipelineBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


private var counter = 0
private var asyncTestCounter = 0

class CommandWithResultHandlerTest {

    init {
        counter = 0
        asyncTestCounter = 0
    }

    @Test
    fun `commandHandler should be fired`() {
        val handler = MyCommandRHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyCommandRHandler::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommand(MyCommandR())

        assertTrue {
            counter == 1
        }
    }

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        val handler = AsyncMyCommandRHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AsyncMyCommandRHandler::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyAsyncCommandR())

        assertTrue {
            asyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command has not been registered before`() {
        val provider = ManuelDependencyProvider(hashMapOf())
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                bus.executeCommandAsync(NonExistCommandR())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommandR")
    }

    @Test
    fun `should throw exception if given command has not been registered before`() {
        val provider = ManuelDependencyProvider(hashMapOf())
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            bus.executeCommand(NonExistCommandR())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommandR")
    }
}

class Result
class NonExistCommandR : Command
class MyCommandR : CommandWithResult<Result>

class MyCommandRHandler : CommandWithResultHandler<MyCommandR, Result> {
    override fun handle(command: MyCommandR): Result {
        counter++

        return Result()
    }
}

class MyAsyncCommandR : CommandWithResult<Result>

class AsyncMyCommandRHandler : AsyncCommandWithResultHandler<MyAsyncCommandR, Result> {
    override suspend fun handleAsync(command: MyAsyncCommandR): Result {
        delay(500)
        asyncTestCounter++

        return Result()
    }
}
