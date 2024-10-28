package com.trendyol.kediatr.framewokUseCases

import com.trendyol.kediatr.HandlerNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

abstract class MediatorUseCases : MediatorDIConvention {
  @Test
  fun command() = runTest {
    val count = 0
    val result = testMediator.send(TestCommandWithResult(count))
    result.value shouldBe count + 1
  }

  @Test
  fun commandWithoutAHandler() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(TestNonExistCommand())
    }

    exception.message shouldBe "handler could not be found for ${TestNonExistCommand::class.java.typeName}"
  }

  @Test
  fun commandWithResult() = runTest {
    val count = 0
    val command = TestCommandWithResult(count)
    val result = testMediator.send(command)
    result.value shouldBe count + 1
    command.invocationCount() shouldBe 1
  }

  @Test
  fun commandWithResultWithoutAHandler() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(NonExistCommandWithResult())
    }

    exception.message shouldBe "handler could not be found for ${NonExistCommandWithResult::class.java.typeName}"
  }

  @Test
  fun notification() = runTest {
    val notification = TestNotification()
    testMediator.publish(notification)
    notification.invocationCount() shouldBe 1
  }

  @Test
  fun pipeline() = runTest {
    val command = TestPipelineCommand()
    testMediator.send(command)
    command.visitedPipelines() shouldBe setOf(
      ExceptionPipelineBehavior::class.simpleName,
      LoggingPipelineBehavior::class.simpleName
    )
  }

  @Test
  fun command_throws_exception() = runTest {
    val act = suspend { testMediator.send(TestBrokenCommand()) }
    assertThrows<Exception> { act() }
  }

  @Test
  fun query_without_handler() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(NonExistQuery())
    }

    exception.message shouldBe "handler could not be found for ${NonExistQuery::class.java.typeName}"
  }

  @Test
  fun query() = runTest {
    val result = testMediator.send(TestQuery(1))
    result shouldBe "hello 1"
  }
}
