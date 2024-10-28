package com.trendyol.kediatr.coreUseCases

import com.trendyol.kediatr.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*

private var counter = 0
private var asyncTestCounter = 0

abstract class CommandWithResultHandlerUseCases : MediatorTestConvention() {
  @BeforeEach
  fun beforeEach() {
    counter = 0
    asyncTestCounter = 0
  }

  @Test
  fun `async commandHandler should be fired`() = runTest {
    val handler = AsyncMyCommandRHandler()
    val bus: Mediator = newMediator(handlers = listOf(handler))
    bus.send(MyAsyncCommandR())
    asyncTestCounter shouldBe 1
  }

  @Test
  fun `should throw exception if given async command has not been registered before`() = runTest {
    val bus: Mediator = newMediator()
    val exception = shouldThrow<HandlerNotFoundException> {
      bus.send(NonExistCommandR())
    }

    exception.message shouldBe "handler could not be found for ${NonExistCommandR::class.java.typeName}"
  }

  @Test
  fun inheritance_should_work_with_command_with_result() = runTest {
    var invocationCount = 0

    class MyAsyncCommand : CommandWithResult<Result>

    class AsyncMyCommandHandler : CommandWithResultHandler<MyAsyncCommand, Result> {
      override suspend fun handle(command: MyAsyncCommand): Result {
        invocationCount++
        return Result()
      }
    }

    val handler = AsyncMyCommandHandler()
    val bus: Mediator = newMediator(handlers = listOf(handler))
    bus.send(MyAsyncCommand())

    invocationCount shouldBe 1
  }

  inner class ParameterizedCommandWithResult<TParam>(val param: TParam) : CommandWithResult<String>

  inner class ParameterizedAsyncCommandWithResultHandler<TParam> :
    CommandWithResultHandler<ParameterizedCommandWithResult<TParam>, String> {
    override suspend fun handle(command: ParameterizedCommandWithResult<TParam>): String {
      counter++
      return command.param.toString()
    }
  }

  @Test
  fun `async commandWithResult should be fired and return result`() = runTest {
    // given
    val handler = ParameterizedAsyncCommandWithResultHandler<ParameterizedCommandWithResult<Long>>()
    val bus: Mediator = newMediator(handlers = listOf(handler))

    // when
    val result = bus.send(ParameterizedCommandWithResult(61L))

    // then
    counter shouldBe 1
    result shouldBe "61"
  }

  @Test
  fun inheritance_should_work_with_command_with_result_and_parameter() = runTest {
    var invocationCount = 0

    class ParameterizedCommandWithResult<TParam>(val param: TParam) : CommandWithResult<String>

    abstract class ParameterizedCommandWithResultHandlerBase<TParam : CommandWithResult<String>> :
      CommandWithResultHandler<TParam, String>

    class Handler<TParam> : ParameterizedCommandWithResultHandlerBase<ParameterizedCommandWithResult<TParam>>() {
      override suspend fun handle(command: ParameterizedCommandWithResult<TParam>): String {
        invocationCount++
        return command.param.toString()
      }
    }

    val handler = Handler<ParameterizedCommandWithResult<Long>>()
    val bus: Mediator = newMediator(handlers = listOf(handler))

    // when
    val result = bus.send(ParameterizedCommandWithResult("invoked"))

    // then
    invocationCount shouldBe 1
    result shouldBe "invoked"
  }
}

private class Result

private class NonExistCommandR : Command

private class MyAsyncCommandR : CommandWithResult<Result>

private class AsyncMyCommandRHandler : CommandWithResultHandler<MyAsyncCommandR, Result> {
  override suspend fun handle(command: MyAsyncCommandR): Result {
    delay(500)
    asyncTestCounter++

    return Result()
  }
}
