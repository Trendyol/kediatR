package com.trendyol.kediatr.coreUseCases

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Mediator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

abstract class CommandHandlerUseCases : MediatorTestConvention() {
  @Test
  fun async_commandHandler_should_be_fired() = runTest {
    var invocationCount = 0

    class MyAsyncCommand : Command

    class MyCommandHandler : CommandHandler<MyAsyncCommand> {
      override suspend fun handle(command: MyAsyncCommand) {
        invocationCount++
      }
    }

    val bus: Mediator = newMediator(handlers = listOf(MyCommandHandler()))
    bus.send(MyAsyncCommand())

    invocationCount shouldBe 1
  }

  @Test
  fun `should throw exception if given async command has not been registered before`() = runTest {
    class NonExistCommand : Command

    val bus: Mediator = newMediator()

    val exception = shouldThrow<HandlerNotFoundException> {
      bus.send(NonExistCommand())
    }

    exception.message shouldBe "handler could not be found for ${NonExistCommand::class.java.typeName}"
  }

  @Test
  fun inheritance_should_work() = runTest {
    var invocationCount = 0

    class MyCommandForInheritance : Command

    abstract class MyCommandHandlerFor<TCommand : Command> : CommandHandler<TCommand>

    class MyInheritedCommandHandler : MyCommandHandlerFor<MyCommandForInheritance>() {
      override suspend fun handle(command: MyCommandForInheritance) {
        invocationCount++
      }
    }

    val handler = MyInheritedCommandHandler()
    val bus: Mediator = newMediator(handlers = listOf(handler))
    bus.send(MyCommandForInheritance())

    invocationCount shouldBe 1
  }

  @Test
  fun inheritance_but_not_parameterized_should_work() = runTest {
    var invocationCount = 0

    class MyCommandForInheritance : Command

    abstract class MyCommandHandlerBaseForSpecificCommand : CommandHandler<MyCommandForInheritance>

    class MyInheritedCommandHandlerForSpecificCommand : MyCommandHandlerBaseForSpecificCommand() {
      override suspend fun handle(command: MyCommandForInheritance) {
        invocationCount++
      }
    }

    val handler = MyInheritedCommandHandlerForSpecificCommand()
    val bus: Mediator = newMediator(handlers = listOf(handler))
    bus.send(MyCommandForInheritance())

    invocationCount shouldBe 1
  }

  @Test
  fun async_command_should_be_fired() = runTest {
    var invocationCount = 0

    class ParameterizedCommand<T>(val param: T) : Command

    class ParameterizedCommandHandler<A> : CommandHandler<ParameterizedCommand<A>> {
      override suspend fun handle(command: ParameterizedCommand<A>) {
        command.param shouldBe "MyParam"
        invocationCount++
      }
    }

    // given
    val handler = ParameterizedCommandHandler<ParameterizedCommand<String>>()
    val bus: Mediator = newMediator(handlers = listOf(handler))

    // when
    bus.send(ParameterizedCommand("MyParam"))

    // then
    invocationCount shouldBe 1
  }

  @Test
  fun async_commandHandler_with_inheritance_should_be_fired() = runTest {
    var invocationCount = 0

    class ParameterizedCommand<T>(val param: T) : Command

    abstract class ParameterizedCommandHandlerBase<A> : CommandHandler<ParameterizedCommand<A>>

    class ParameterizedCommandHandler<A> : ParameterizedCommandHandlerBase<A>() {
      override suspend fun handle(command: ParameterizedCommand<A>) {
        command.param shouldBe "MyParam"
        invocationCount++
      }
    }

    // given
    val handler = ParameterizedCommandHandler<ParameterizedCommand<String>>()
    val bus: Mediator = newMediator(handlers = listOf(handler))

    // when
    bus.send(ParameterizedCommand("MyParam"))

    // then
    invocationCount shouldBe 1
  }
}
