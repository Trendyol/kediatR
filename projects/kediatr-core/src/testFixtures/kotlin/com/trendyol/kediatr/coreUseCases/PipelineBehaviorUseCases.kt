package com.trendyol.kediatr.coreUseCases

import com.trendyol.kediatr.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*

var exceptionPipelineBehaviorHandleCounter = 0
var exceptionPipelineBehaviorHandleCatchCounter = 0
var loggingPipelineBehaviorHandleBeforeNextCounter = 0
var loggingPipelineBehaviorHandleAfterNextCounter = 0
var inheritedPipelineBehaviourHandleCounter = 0
var commandTestCounter = 0

abstract class PipelineBehaviorUseCases : MediatorTestConvention() {
  @BeforeEach
  fun beforeEach() {
    exceptionPipelineBehaviorHandleCounter = 0
    exceptionPipelineBehaviorHandleCatchCounter = 0
    loggingPipelineBehaviorHandleBeforeNextCounter = 0
    loggingPipelineBehaviorHandleAfterNextCounter = 0
    inheritedPipelineBehaviourHandleCounter = 0
    commandTestCounter = 0
  }

  private class MyCommand : Command

  private class MyCommandHandler : CommandHandler<MyCommand> {
    override suspend fun handle(command: MyCommand) {
      commandTestCounter++
      delay(500)
    }
  }

  @Test
  fun `should process command without async pipeline`() = runTest {
    val handler = MyCommandHandler()
    val mediator: Mediator = newMediator(handlers = listOf(handler))
    mediator.send(MyCommand())

    commandTestCounter shouldBe 1
    exceptionPipelineBehaviorHandleCatchCounter shouldBe 0
    exceptionPipelineBehaviorHandleCounter shouldBe 0
    loggingPipelineBehaviorHandleBeforeNextCounter shouldBe 0
    loggingPipelineBehaviorHandleAfterNextCounter shouldBe 0
  }

  @Test
  fun `should process command with async pipeline`() = runTest {
    val handler = MyCommandHandler()
    val exceptionPipeline = ExceptionPipelineBehavior()
    val loggingPipeline = LoggingPipelineBehavior()
    val bus: Mediator = newMediator(handlers = listOf(exceptionPipeline, loggingPipeline) + listOf(handler))

    bus.send(MyCommand())

    commandTestCounter shouldBe 1
    exceptionPipelineBehaviorHandleCatchCounter shouldBe 0
    exceptionPipelineBehaviorHandleCounter shouldBe 1
    loggingPipelineBehaviorHandleBeforeNextCounter shouldBe 1
    loggingPipelineBehaviorHandleAfterNextCounter shouldBe 1
  }

  @Test
  fun `should process exception in async handler`() = runTest {
    val handler = MyBrokenHandler()
    val exceptionPipeline = ExceptionPipelineBehavior()
    val loggingPipeline = LoggingPipelineBehavior()
    val bus: Mediator = newMediator(handlers = listOf(handler) + listOf(exceptionPipeline, loggingPipeline))
    val act = suspend { bus.send(MyBrokenCommand()) }

    shouldThrow<Exception> { act() }
    commandTestCounter shouldBe 0
    exceptionPipelineBehaviorHandleCatchCounter shouldBe 1
    exceptionPipelineBehaviorHandleCounter shouldBe 1
    loggingPipelineBehaviorHandleBeforeNextCounter shouldBe 1
    loggingPipelineBehaviorHandleAfterNextCounter shouldBe 0
  }

  @Test
  fun `should process command with inherited pipeline`() = runTest {
    val handler = MyCommandHandler()
    val pipeline = InheritedPipelineBehaviour()
    val bus: Mediator = newMediator(handlers = listOf(handler) + listOf(pipeline))
    bus.send(MyCommand())

    inheritedPipelineBehaviourHandleCounter shouldBe 1
  }
}

private abstract class MyBasePipelineBehaviour : PipelineBehavior

private class InheritedPipelineBehaviour : MyBasePipelineBehaviour() {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    inheritedPipelineBehaviourHandleCounter++
    return next(request)
  }
}

private class MyBrokenCommand : Command

private class MyBrokenHandler : CommandHandler<MyBrokenCommand> {
  override suspend fun handle(command: MyBrokenCommand) {
    throw Exception()
  }
}

private class ExceptionPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    try {
      exceptionPipelineBehaviorHandleCounter++
      return next(request)
    } catch (ex: Exception) {
      exceptionPipelineBehaviorHandleCatchCounter++
      throw ex
    }
  }
}

private class LoggingPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    loggingPipelineBehaviorHandleBeforeNextCounter++
    val result = next(request)
    loggingPipelineBehaviorHandleAfterNextCounter++
    return result
  }
}
