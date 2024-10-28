package com.trendyol.kediatr.framewokUseCases

import com.trendyol.kediatr.HandlerNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*

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
    val result = testMediator.send(TestCommandWithResult(count))
    result.value shouldBe count + 1
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
    testMediator.publish(TestNotification())
  }

  @Test
  fun `should process command with async pipeline`() = runTest {
    testMediator.send(TestPipelineCommand())
  }

  @Test
  fun `should process exception in async handler`() = runTest {
    val act = suspend { testMediator.send(TestBrokenCommand()) }
    assertThrows<Exception> { act() }
  }

  @Test
  fun `should throw exception if given async query does not have handler bean`() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(NonExistQuery())
    }

    exception.message shouldBe "handler could not be found for ${NonExistQuery::class.java.typeName}"
  }

  @Test
  fun `should retrieve result from async query handler bean`() = runTest {
    val result = testMediator.send(TestQuery(1))

    result shouldBe "hello 1"
  }
}
