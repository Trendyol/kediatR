package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.*

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

    @Test
    fun `inheritance should work`() = runBlocking {
        val handler = MyInheritedAsyncCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyInheritedAsyncCommandHandler::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyCommandForInheritance())

        assertTrue {
            asyncTestCounter == 1
        }
    }

    @Test
    fun `inheritance but not parameterized should work`() = runBlocking {
        val handler = MyInheritedAsyncCommandHandlerForSpecificCommand()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyInheritedAsyncCommandHandlerForSpecificCommand::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyCommandForInheritance())

        assertTrue {
            asyncTestCounter == 1
        }
    }

    @Nested
    inner class ParameterizedTests {
        init {
            counter = 0
            asyncTestCounter = 0
        }

        inner class ParameterizedCommand<T>(val param: T) : Command

        inner class ParameterizedAsyncCommandHandler<A> : AsyncCommandHandler<ParameterizedCommand<A>> {
            override suspend fun handleAsync(command: ParameterizedCommand<A>) {
                counter++
            }
        }

        inner class ParameterizedCommandHandler<A> : CommandHandler<ParameterizedCommand<A>> {
            override fun handle(command: ParameterizedCommand<A>) {
                counter++
            }
        }

        @Test
        fun `async command should be fired`() = runBlocking {
            // given
            val handler = ParameterizedAsyncCommandHandler<ParameterizedCommand<String>>()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(Pair(ParameterizedAsyncCommandHandler::class.java, handler))
            val provider = ManuelDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            bus.executeCommandAsync(ParameterizedCommand("MyParam"))

            // then
            assertTrue {
                counter == 1
            }
        }

        @Test
        fun `command should be fired`() {
            // given
            val handler = ParameterizedCommandHandler<ParameterizedCommand<String>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedCommandHandler::class.java, handler))
            val provider = ManuelDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            bus.executeCommand(ParameterizedCommand("MyParam"))

            // then
            assertTrue {
                counter == 1
            }
        }
    }
}

class NonExistCommand : Command

class MyCommand : Command

class MyCommandHandler() : CommandHandler<MyCommand> {
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

class MyCommandForInheritance : Command
abstract class MyAsyncCommandHandlerFor<TCommand : Command> : AsyncCommandHandler<TCommand>

class MyInheritedAsyncCommandHandler : MyAsyncCommandHandlerFor<MyCommandForInheritance>() {
    override suspend fun handleAsync(command: MyCommandForInheritance) {
        asyncTestCounter++
    }
}
abstract class MyAsyncCommandHandlerBaseForSpecificCommand : AsyncCommandHandler<MyCommandForInheritance>

class MyInheritedAsyncCommandHandlerForSpecificCommand : MyAsyncCommandHandlerBaseForSpecificCommand() {
    override suspend fun handleAsync(command: MyCommandForInheritance) {
        asyncTestCounter++
    }
}
