package com.trendyol.kediatr.testing

import com.trendyol.kediatr.HandlerNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*

abstract class MediatorUseCases : MediatorTestConvention() {
  @Test
  fun command_should_be_routed_to_its_handler() = runTest {
    val command = TestCommand()
    testMediator.send(command)
    command.invocationCount() shouldBe 1
  }

  @Test
  fun command_with_result_should_be_routed_to_its_handler() = runTest {
    val count = 0
    val result = testMediator.send(TestCommandWithResult(count))
    result.value shouldBe count + 1
  }

  @Test
  fun command_without_a_handler_should_throw_HandlerNotFoundException() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(TestNonExistCommand())
    }

    exception.message shouldBe "handler could not be found for ${TestNonExistCommand::class.java.typeName}"
  }

  @Test
  fun command_with_result_that_does_not_have_a_handler_should_throw_HandlerNotFoundException() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(NonExistCommandWithResult())
    }

    exception.message shouldBe "handler could not be found for ${NonExistCommandWithResult::class.java.typeName}"
  }

  @Test
  fun notification_should_be_routed() = runTest {
    val notification = TestNotification()
    testMediator.publish(notification)
    notification.invocationCount() shouldBe 1
  }

  @Test
  fun command_that_has_pipeline_behaviours_should_pass_through_the_pipeline() = runTest {
    val command = CommandThatPassesThroughPipelineBehaviours()
    testMediator.send(command)
    command.visitedPipelines() shouldBe setOf(
      ExceptionPipelineBehavior::class.simpleName,
      LoggingPipelineBehavior::class.simpleName,
      InheritedPipelineBehaviour::class.simpleName
    )
  }

  @Test
  fun command_that_fails_should_throw_related_exception_when_it_is_routed() = runTest {
    val act = suspend { testMediator.send(TestCommandThatFailsWithException()) }
    assertThrows<Exception> { act() }
  }

  @Test
  fun query_without_a_handler_should_throw_HandlerNotFoundException() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(NonExistQuery())
    }

    exception.message shouldBe "handler could not be found for ${NonExistQuery::class.java.typeName}"
  }

  @Test
  fun query_should_be_routed_to_its_handler() = runTest {
    val result = testMediator.send(TestQuery(1))
    result shouldBe "hello 1"
  }

  @Test
  fun inherited_command_handlers_should_work() = runTest {
    val command = TestCommandForInheritance()
    testMediator.send(command)
    command.invocationCount() shouldBe 1
  }

  @Test
  fun inherited_command_should_work() = runTest {
    val command = TestCommandBase.TestCommandInherited1("id")
    testMediator.send(command)
    command.invocationCount() shouldBe 1

    val command2 = TestCommandBase.TestCommandInherited2("id")
    testMediator.send(command2)
    command2.invocationCount() shouldBe 1
  }

  @Test
  fun inherited_command_with_fallback_gives_priority_to_its_handlers_otherwise_to_fallback_handler() = runTest {
    val command = TestCommandForInheritanceWithFallback.TestCommandInherited1("id")
    testMediator.send(command)
    command.invocationCount() shouldBe 1
    command.whereItWasInvokedFrom() shouldBe TestCommandForInheritanceWithFallbackHandlerHandler::class.java.name

    val command2 = TestCommandForInheritanceWithFallback.TestCommandInherited2("id")
    testMediator.send(command2)
    command2.invocationCount() shouldBe 1
    command2.whereItWasInvokedFrom() shouldBe TestCommandHandlerForCommandInherited2::class.java.name
  }

  @Test
  fun inherited_query_should_work() = runTest {
    val query = TestQueryBase.TestQueryInherited1("id")
    testMediator.send(query)
    query.invocationCount() shouldBe 1

    val query2 = TestQueryBase.TestQueryInherited2("id2")
    testMediator.send(query2)
    query2.invocationCount() shouldBe 1
  }

  @Test
  fun inherited_command_with_result_should_work() = runTest {
    val command = TestCommandWithResultBase.TestCommandWithResultInherited1("id1")
    val result = testMediator.send(command)
    result shouldBe "id1 handled"
    command.invocationCount() shouldBe 1

    val command2 = TestCommandWithResultBase.TestCommandWithResultInherited2("id2")
    val result2 = testMediator.send(command2)
    result2 shouldBe "id2 handled"
    command2.invocationCount() shouldBe 1
  }

  @Test
  fun command_is_routed_to_its_handler() = runTest {
    val command = TestCommandForWithoutInjection()
    testMediator.send(command)
    command.invocationCount() shouldBe 1
  }

  @Test
  fun throws_exception_for_command_that_does_not_have_a_handler() = runTest {
    val exception = shouldThrow<HandlerNotFoundException> {
      testMediator.send(TestNonExistCommand())
    }

    exception.message shouldBe "handler could not be found for ${TestNonExistCommand::class.java.typeName}"
  }

  @Test
  fun inherited_type_limited_commandHandlers_should_handle_the_commands() = runTest {
    val command = TestCommandForTypeLimitedInheritance()
    testMediator.send(command)
    command.invocationCount() shouldBe 1
  }

  @Test
  fun inherited_commandHandlers_should_handle_the_commands() = runTest {
    val command = TestCommandForInheritance()
    testMediator.send(command)
    command.invocationCount() shouldBe 1
  }

  @Test
  fun generic_parameterized_command_class_should_be_routed() = runTest {
    val command = ParameterizedCommand("MyParam")
    testMediator.send(command)
    command.invocationCount() shouldBe 1
  }

  @Test
  fun generic_parameterized_command_class_should_be_routed_to_inherited_handler() = runTest {
    val command = ParameterizedCommandForInheritedCommandHandler("Netherlands")
    testMediator.send(command)
    command.invocationCount() shouldBe 1
  }

  @Test
  fun generic_parameterized_commandWithResult_should_be_handled_and_return_result() = runTest {
    val command = ParameterizedCommandWithResult<Long, String>(10) {
      (it + 1).toString()
    }
    val result = testMediator.send(command)
    command.invocationCount() shouldBe 1
    result shouldBe "11"
  }

  @Test
  fun generic_parameterized_inheritance_should_work_with_command_with_result_and_parameter() = runTest {
    val command = ParameterizedCommandWithResultForInheritance("invoked")
    val result = testMediator.send(command)
    command.invocationCount() shouldBe 1
    result shouldBe "invoked"
  }

  @Test
  fun inherited_notification_should_be_called_for_its_direct_handler_and_indirect_handler() = runTest {
    val notification = ExtendedPing()
    testMediator.publish(notification)
    notification.invocationCount() shouldBe 2
  }

  @Test
  fun notification_that_is_handled_in_multiple_handlers_should_be_dispatched() = runTest {
    val notification = NotificationForMultipleHandlers()
    testMediator.publish(notification)
    notification.invocationCount() shouldBe 2
  }

  @Test
  fun inherited_notification_handler_should_be_called() = runTest {
    val notification = PingForInherited()
    testMediator.publish(notification)
    notification.invocationCount() shouldBe 1
  }

  @Test
  fun generic_parameterized_notification_should_be_routed() = runTest {
    val notification = ParameterizedNotification("My Param")
    testMediator.publish(notification)
    notification.invocationCount() shouldBe 1
  }

  @Test
  fun generic_parameterized_inherited_notification_handler_should_be_called_with_param() = runTest {
    val notification = ParameterizedNotificationForInheritance("My Param")
    testMediator.publish(notification)
    notification.invocationCount() shouldBe 1
  }

  @Test
  fun should_process_exception_in_handler() = runTest {
    val act = suspend { testMediator.send(CommandThatFailsWhilePassingThroughPipelineBehaviours()) }
    shouldThrow<Exception> { act() }
  }

  @Test
  fun should_process_command_with_inherited_pipeline() = runTest {
    val command = CommandForWithoutInjectionThatPassesThroughPipelineBehaviours()
    testMediator.send(command)
    command.visitedPipelines() shouldBe setOf(
      ExceptionPipelineBehavior::class.simpleName,
      LoggingPipelineBehavior::class.simpleName,
      InheritedPipelineBehaviour::class.simpleName
    )
  }

  @Test
  fun generic_parameterized_query_should_be_routed_to_its_handler() = runTest {
    val query = ParameterizedQuery(10L) {
      (it * 6).toString()
    }
    val result = testMediator.send(query)
    result shouldBe "60"
    query.invocationCount() shouldBe 1
  }

  @Test
  fun ordered_pipeline_behaviours_should_be_executed_in_order_for_command() = runTest {
    val command = CommandThatPassesThroughOrderedPipelineBehaviours()
    testMediator.send(command)
    command.visitedPipelines() shouldBe listOf(
      FirstPipelineBehaviour::class.simpleName,
      SecondPipelineBehaviour::class.simpleName,
      ThirdPipelineBehaviour::class.simpleName
    )
  }

  @Test
  fun ordered_pipeline_behaviours_should_be_executed_in_order_for_query() = runTest {
    val query = QueryThatPassesThroughOrderedPipelineBehaviours()
    testMediator.send(query)
    query.visitedPipelines() shouldBe listOf(
      FirstPipelineBehaviour::class.simpleName,
      SecondPipelineBehaviour::class.simpleName,
      ThirdPipelineBehaviour::class.simpleName
    )
  }

  @Test
  fun ordered_pipeline_behaviours_should_be_executed_in_order_for_notification() = runTest {
    val notification = NotificationThatPassesThroughOrderedPipelineBehaviours()
    testMediator.publish(notification)
    notification.visitedPipelines() shouldBe listOf(
      FirstPipelineBehaviour::class.simpleName,
      SecondPipelineBehaviour::class.simpleName,
      ThirdPipelineBehaviour::class.simpleName
    )
  }
}
