package com.trendyol.kediatr

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CommandHandlerTest {
    @Test
    fun async_commandHandler_should_be_fired() =
        runBlocking {
            class MyAsyncCommand : Command

            class MyCommandHandler : CommandHandler<MyAsyncCommand> {
                var invocationCount = 0

                override suspend fun handle(command: MyAsyncCommand) {
                    invocationCount++
                }
            }

            val handler = MyCommandHandler()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyCommandHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: Mediator = MediatorBuilder(provider).build()
            bus.send(MyAsyncCommand())

            assertEquals(1, handler.invocationCount)
        }

    @Test
    fun `should throw exception if given async command has not been registered before`() {
        class NonExistCommand : Command

        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()

        val exception =
            assertFailsWith(HandlerNotFoundException::class) {
                runBlocking {
                    bus.send(NonExistCommand())
                }
            }

        assertNotNull(exception)
        assertEquals("handler could not be found for ${NonExistCommand::class.java.typeName}", exception.message)
    }

    @Test
    fun inheritance_should_work() =
        runBlocking {
            class MyCommandForInheritance : Command

            abstract class MyCommandHandlerFor<TCommand : Command> : CommandHandler<TCommand>

            class MyInheritedCommandHandler : MyCommandHandlerFor<MyCommandForInheritance>() {
                var invocationCount = 0

                override suspend fun handle(command: MyCommandForInheritance) {
                    invocationCount++
                }
            }

            val handler = MyInheritedCommandHandler()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyInheritedCommandHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: Mediator = MediatorBuilder(provider).build()
            bus.send(MyCommandForInheritance())

            assertEquals(1, handler.invocationCount)
        }

    @Test
    fun inheritance_but_not_parameterized_should_work() =
        runBlocking {
            class MyCommandForInheritance : Command

            abstract class MyCommandHandlerBaseForSpecificCommand : CommandHandler<MyCommandForInheritance>

            class MyInheritedCommandHandlerForSpecificCommand : MyCommandHandlerBaseForSpecificCommand() {
                var invocationCount = 0

                override suspend fun handle(command: MyCommandForInheritance) {
                    invocationCount++
                }
            }

            val handler = MyInheritedCommandHandlerForSpecificCommand()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(Pair(MyInheritedCommandHandlerForSpecificCommand::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: Mediator = MediatorBuilder(provider).build()
            bus.send(MyCommandForInheritance())

            assertEquals(1, handler.invocationCount)
        }

    @Nested
    inner class ParameterizedTests {
        @Test
        fun async_command_should_be_fired() =
            runBlocking {
                class ParameterizedCommand<T>(val param: T) : Command

                class ParameterizedCommandHandler<A> : CommandHandler<ParameterizedCommand<A>> {
                    var invocationCount = 0

                    override suspend fun handle(command: ParameterizedCommand<A>) {
                        invocationCount++
                    }
                }

                // given
                val handler = ParameterizedCommandHandler<ParameterizedCommand<String>>()
                val handlers: HashMap<Class<*>, Any> =
                    hashMapOf(Pair(ParameterizedCommandHandler::class.java, handler))
                val provider = ManualDependencyProvider(handlers)
                val bus: Mediator = MediatorBuilder(provider).build()

                // when
                bus.send(ParameterizedCommand("MyParam"))

                // then
                assertEquals(1, handler.invocationCount)
            }

        @Test
        fun async_commandHandler_with_inheritance_should_be_fired() =
            runBlocking {
                class ParameterizedCommand<T>(val param: T) : Command

                abstract class ParameterizedCommandHandlerBase<A> : CommandHandler<ParameterizedCommand<A>>

                class ParameterizedCommandHandler<A> : ParameterizedCommandHandlerBase<A>() {
                    var invocationCount = 0

                    override suspend fun handle(command: ParameterizedCommand<A>) {
                        invocationCount++
                    }
                }

                // given
                val handler = ParameterizedCommandHandler<ParameterizedCommand<String>>()
                val handlers: HashMap<Class<*>, Any> =
                    hashMapOf(Pair(ParameterizedCommandHandler::class.java, handler))
                val provider = ManualDependencyProvider(handlers)
                val bus: Mediator = MediatorBuilder(provider).build()

                // when
                bus.send(ParameterizedCommand("MyParam"))

                // then
                assertEquals(1, handler.invocationCount)
            }
    }
}
