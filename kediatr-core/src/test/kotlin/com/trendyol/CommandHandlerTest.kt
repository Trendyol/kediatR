package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CommandHandlerTest {

    @Test
    fun commandHandler_should_be_fired() {
        class TestCommand : Command

        class TestCommandHandler : CommandHandler<TestCommand> {
            var invocationCount = 0
            override fun handle(command: TestCommand) {
                invocationCount++
            }
        }

        val handler = TestCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(TestCommandHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommand(TestCommand())

        assertEquals(1, handler.invocationCount)
    }

    @Test
    fun async_commandHandler_should_be_fired() = runBlocking {
        class MyAsyncCommand : Command

        class AsyncMyCommandHandler : AsyncCommandHandler<MyAsyncCommand> {
            var invocationCount = 0
            override suspend fun handleAsync(command: MyAsyncCommand) {
                delay(500)
                invocationCount++
            }
        }

        val handler = AsyncMyCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AsyncMyCommandHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyAsyncCommand())

        assertEquals(1, handler.invocationCount)
    }

    @Test
    fun `should throw exception if given async command has not been registered before`() {
        class NonExistCommand : Command

        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                bus.executeCommandAsync(NonExistCommand())
            }
        }

        assertNotNull(exception)
        assertEquals("handler could not be found for ${NonExistCommand::class.java.typeName}", exception.message)
    }

    @Test
    fun `should throw exception if given command has not been registered before`() {
        class NonExistCommand : Command

        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            bus.executeCommand(NonExistCommand())
        }

        assertNotNull(exception)
        assertEquals("handler could not be found for ${NonExistCommand::class.java.typeName}", exception.message)
    }

    @Test
    fun inheritance_should_work() = runBlocking {
        class MyCommandForInheritance : Command
        abstract class MyAsyncCommandHandlerFor<TCommand : Command> : AsyncCommandHandler<TCommand>

        class MyInheritedAsyncCommandHandler : MyAsyncCommandHandlerFor<MyCommandForInheritance>() {
            var invocationCount = 0
            override suspend fun handleAsync(command: MyCommandForInheritance) {
                invocationCount++
            }
        }

        val handler = MyInheritedAsyncCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyInheritedAsyncCommandHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyCommandForInheritance())

        assertEquals(1, handler.invocationCount)
    }

    @Test
    fun inheritance_but_not_parameterized_should_work() = runBlocking {
        class MyCommandForInheritance : Command

        abstract class MyAsyncCommandHandlerBaseForSpecificCommand : AsyncCommandHandler<MyCommandForInheritance>

        class MyInheritedAsyncCommandHandlerForSpecificCommand : MyAsyncCommandHandlerBaseForSpecificCommand() {
            var invocationCount = 0
            override suspend fun handleAsync(command: MyCommandForInheritance) {
                invocationCount++
            }
        }

        val handler = MyInheritedAsyncCommandHandlerForSpecificCommand()
        val handlers: HashMap<Class<*>, Any> =
            hashMapOf(Pair(MyInheritedAsyncCommandHandlerForSpecificCommand::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyCommandForInheritance())

        assertEquals(1, handler.invocationCount)
    }

    @Nested
    inner class ParameterizedTests {

        @Test
        fun async_command_should_be_fired() = runBlocking {
            class ParameterizedCommand<T>(val param: T) : Command

            class ParameterizedAsyncCommandHandler<A> : AsyncCommandHandler<ParameterizedCommand<A>> {
                var invocationCount = 0
                override suspend fun handleAsync(command: ParameterizedCommand<A>) {
                    invocationCount++
                }
            }

            // given
            val handler = ParameterizedAsyncCommandHandler<ParameterizedCommand<String>>()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(Pair(ParameterizedAsyncCommandHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            bus.executeCommandAsync(ParameterizedCommand("MyParam"))

            // then
            assertEquals(1, handler.invocationCount)
        }

        @Test
        fun async_commandHandler_with_inheritance_should_be_fired() = runBlocking {
            class ParameterizedCommand<T>(val param: T) : Command

            abstract class ParameterizedCommandHandlerBase<A> : AsyncCommandHandler<ParameterizedCommand<A>>

            class ParameterizedAsyncCommandHandler<A> : ParameterizedCommandHandlerBase<A>() {
                var invocationCount = 0
                override suspend fun handleAsync(command: ParameterizedCommand<A>) {
                    invocationCount++
                }
            }

            // given
            val handler = ParameterizedAsyncCommandHandler<ParameterizedCommand<String>>()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(Pair(ParameterizedAsyncCommandHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            bus.executeCommandAsync(ParameterizedCommand("MyParam"))

            // then
            assertEquals(1, handler.invocationCount)
        }

        @Test
        fun command_should_be_fired() {
            class ParameterizedCommand<T>(val param: T) : Command
            class ParameterizedCommandHandler<A> : CommandHandler<ParameterizedCommand<A>> {
                var invocationCount = 0
                override fun handle(command: ParameterizedCommand<A>) {
                    invocationCount++
                }
            }

            // given
            val handler = ParameterizedCommandHandler<ParameterizedCommand<String>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedCommandHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            bus.executeCommand(ParameterizedCommand("MyParam"))

            // then
            assertEquals(1, handler.invocationCount)
        }
    }
}
