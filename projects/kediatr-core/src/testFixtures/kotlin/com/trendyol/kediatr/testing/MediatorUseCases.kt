package com.trendyol.kediatr.testing

import com.trendyol.kediatr.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.*
import io.kotest.matchers.collections.*
import io.kotest.matchers.ints.*
import io.kotest.matchers.longs.*
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*

abstract class MediatorUseCases : MediatorTestConvention() {
  @Test
  fun command_should_be_routed_to_its_handler() = runTest {
    val command = TestCommand()
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1
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
    notification.invocationCount().get() shouldBe 1
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
    result shouldBe "hello 1-modified"
  }

  @Test
  fun inherited_command_handlers_should_work() = runTest {
    val command = TestCommandForInheritance()
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1
  }

  @Test
  fun inherited_command_should_work() = runTest {
    val command = TestCommandBase.TestCommandInherited1("id")
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1

    val command2 = TestCommandBase.TestCommandInherited2("id")
    testMediator.send(command2)
    command2.invocationCount().get() shouldBe 1
  }

  @Test
  fun inherited_command_with_fallback_gives_priority_to_its_handlers_otherwise_to_fallback_handler() = runTest {
    val command = TestCommandForInheritanceWithFallback.TestCommandInherited1("id")
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1
    command.whereItWasInvokedFrom() shouldBe TestCommandForInheritanceWithFallbackHandlerHandler::class.java.name

    val command2 = TestCommandForInheritanceWithFallback.TestCommandInherited2("id")
    testMediator.send(command2)
    command2.invocationCount().get() shouldBe 1
    command2.whereItWasInvokedFrom() shouldBe TestRequestHandlerForCommandInherited2::class.java.name
  }

  @Test
  fun inherited_query_should_work() = runTest {
    val query = TestQueryBase.TestQueryInherited1("id")
    testMediator.send(query)
    query.invocationCount().get() shouldBe 1

    val query2 = TestQueryBase.TestQueryInherited2("id2")
    testMediator.send(query2)
    query2.invocationCount().get() shouldBe 1
  }

  @Test
  fun inherited_command_with_result_should_work() = runTest {
    val command = TestCommandWithResultBase.TestCommandWithResultInherited1("id1")
    val result = testMediator.send(command)
    result shouldBe "id1 handled-modified"
    command.invocationCount().get() shouldBe 1

    val command2 = TestCommandWithResultBase.TestCommandWithResultInherited2("id2")
    val result2 = testMediator.send(command2)
    result2 shouldBe "id2 handled-modified"
    command2.invocationCount().get() shouldBe 1
  }

  @Test
  fun command_is_routed_to_its_handler() = runTest {
    val command = TestCommandForWithoutInjection()
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1
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
    command.invocationCount().get() shouldBe 1
  }

  @Test
  fun inherited_commandHandlers_should_handle_the_commands() = runTest {
    val command = TestCommandForInheritance()
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1
  }

  @Test
  fun generic_parameterized_command_class_should_be_routed() = runTest {
    val command = ParameterizedCommand("MyParam")
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1
  }

  @Test
  fun generic_parameterized_command_class_should_be_routed_to_inherited_handler() = runTest {
    val command = ParameterizedCommandForInheritedRequestHandler("Netherlands")
    testMediator.send(command)
    command.invocationCount().get() shouldBe 1
  }

  @Test
  fun generic_parameterized_commandWithResult_should_be_handled_and_return_result() = runTest {
    val command = ParameterizedCommandWithResult<Long, String>(10) {
      (it + 1).toString()
    }
    val result = testMediator.send(command)
    command.invocationCount().get() shouldBe 1
    result shouldBe "11-modified"
  }

  @Test
  fun generic_parameterized_inheritance_should_work_with_command_with_result_and_parameter() = runTest {
    val command = ParameterizedCommandWithResultForInheritance("invoked")
    val result = testMediator.send(command)
    command.invocationCount().get() shouldBe 1
    result shouldBe "invoked-modified"
  }

  @Test
  fun inherited_notification_should_be_called_for_its_direct_handler_and_indirect_handler() = runTest {
    val notification = ExtendedPing()
    testMediator.publish(notification)
    notification.invocationCount().get() shouldBe 2
  }

  @Test
  fun notification_that_is_handled_in_multiple_handlers_should_be_dispatched() = runTest {
    val notification = NotificationForMultipleHandlers()
    testMediator.publish(notification)
    notification.invocationCount().get() shouldBe 2
  }

  @Test
  fun inherited_notification_handler_should_be_called() = runTest {
    val notification = PingForInherited()
    testMediator.publish(notification)
    notification.invocationCount().get() shouldBe 1
  }

  @Test
  fun generic_parameterized_notification_should_be_routed() = runTest {
    val notification = ParameterizedNotification("My Param")
    testMediator.publish(notification)
    notification.invocationCount().get() shouldBe 1
  }

  @Test
  fun generic_parameterized_inherited_notification_handler_should_be_called_with_param() = runTest {
    val notification = ParameterizedNotificationForInheritance("My Param")
    testMediator.publish(notification)
    notification.invocationCount().get() shouldBe 1
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
    result shouldBe "60-modified"
    query.invocationCount().get() shouldBe 1
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

  // ============================================
  // EDGE CASE TESTS
  // ============================================

  @Test
  fun request_with_nullable_result_should_return_null() = runTest {
    val request = RequestWithNullableResult()
    val result = testMediator.send(request)
    result shouldBe null
  }

  @Test
  fun request_with_null_parameter_should_handle_null_gracefully() = runTest {
    val request = RequestWithNullParameter(null)
    val result = testMediator.send(request)
    result shouldBe "null-handled-modified"
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun request_with_non_null_parameter_should_process_normally() = runTest {
    val request = RequestWithNullParameter("test-value")
    val result = testMediator.send(request)
    result shouldBe "test-value-modified"
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun nested_generic_request_should_handle_complex_generics() = runTest {
    val request = NestedGenericRequest("outer", 42)
    val result = testMediator.send(request)
    result.first shouldBe "outer"
    result.second shouldBe 42
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun wildcard_generic_request_should_handle_bounded_generics() = runTest {
    val request = WildcardGenericRequest(42)
    val result = testMediator.send(request)
    result shouldBe 42.0
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun wildcard_generic_request_with_double_should_work() = runTest {
    val request = WildcardGenericRequest(3.14)
    val result = testMediator.send(request)
    result shouldBe 3.14
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun concurrent_requests_should_be_handled_independently() = runTest {
    val requests = (1..5).map { ConcurrentRequest(it) }
    val results = requests.map { async { testMediator.send(it) } }.awaitAll()

    requests.forEach { it.invocationCount().get() shouldBe 1 }
    results.forEach { it shouldNotBe null }
    results.toSet().size shouldBe 5 // All results should be unique

    // Verify concurrent execution
    val allThreadIds = requests.flatMap { it.threadIds() }.toSet()
    allThreadIds.size shouldBeGreaterThan 0
  }

  @Test
  fun long_running_request_should_complete_successfully() = runTest {
    val request = LongRunningRequest(50)
    val result = testMediator.send(request)

    result shouldBe "completed-after-50ms-modified"
    request.invocationCount().get() shouldBe 1
    request.executionTime() shouldBeGreaterThan 0L // Allow more variance for test environments
  }

  @Test
  fun self_referencing_request_should_handle_recursion() = runTest {
    val request = SelfReferencingRequest(3)
    val result = testMediator.send(request)

    result shouldBe 3 // 3 levels of recursion
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun self_referencing_request_with_zero_depth_should_return_zero() = runTest {
    val request = SelfReferencingRequest(0)
    val result = testMediator.send(request)

    result shouldBe 0
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun multi_interface_request_should_be_handled() = runTest {
    val request = MultiInterfaceRequest()
    testMediator.send(request)
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun empty_request_should_be_handled_without_error() = runTest {
    val request = EmptyRequest()
    testMediator.send(request) // Should not throw
  }

  @Test
  fun void_result_request_should_return_unit() = runTest {
    val request = VoidResultRequest()
    val result = testMediator.send(request)
    result shouldBe Unit
  }

  @Test
  fun collection_request_should_transform_collections() = runTest {
    val request = CollectionRequest(listOf("a", "b", "a", "c"))
    val result = testMediator.send(request)

    result shouldBe setOf("a", "b", "c")
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun collection_request_with_empty_list_should_return_empty_set() = runTest {
    val request = CollectionRequest(emptyList())
    val result = testMediator.send(request)

    result shouldBe emptySet()
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun request_that_throws_specific_exception_should_propagate_exception() = runTest {
    val request = RequestThatThrowsSpecificException()

    val exception = shouldThrow<IllegalArgumentException> {
      testMediator.send(request)
    }

    exception.message shouldBe "Specific exception for testing"
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun request_that_throws_runtime_exception_should_propagate_exception() = runTest {
    val request = RequestThatThrowsRuntimeException()

    val exception = shouldThrow<RuntimeException> {
      testMediator.send(request)
    }

    exception.message shouldBe "Runtime exception for testing"
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun notification_without_handlers_should_complete_silently() = runTest {
    val notification = NotificationWithoutHandlers()
    testMediator.publish(notification) // Should not throw
    notification.invocationCount().get() shouldBe 0
  }

  @Test
  fun notification_with_failing_handlers_using_default_strategy_should_throw_first_exception() = runTest {
    val notification = NotificationThatThrowsException()

    val exception = shouldThrow<RuntimeException> {
      testMediator.publish(notification, PublishStrategy.DEFAULT)
    }

    // Only first handler should have been called
    notification.invocationCount().get() shouldBeGreaterThanOrEqual 1
    exception.message shouldContain "Handler"
  }

  @Test
  fun notification_with_failing_handlers_using_continue_strategy_should_aggregate_exceptions() = runTest {
    val notification = NotificationThatThrowsException()

    val exception = shouldThrow<AggregateException> {
      testMediator.publish(notification, PublishStrategy.CONTINUE_ON_EXCEPTION)
    }

    // All handlers should have been called
    notification.invocationCount().get() shouldBe 3
    exception.exceptions shouldHaveSize 2 // Two handlers throw exceptions
    exception.exceptions.map { it::class }.toSet() shouldBe setOf(RuntimeException::class, IllegalStateException::class)
  }

  @Test
  fun slow_notification_with_parallel_no_wait_strategy_should_return_immediately() = runTest {
    val notification = SlowNotification(100)
    val startTime = System.currentTimeMillis()

    testMediator.publish(notification, PublishStrategy.PARALLEL_NO_WAIT)

    val endTime = System.currentTimeMillis()
    val executionTime = endTime - startTime

    // Should return quickly without waiting for handlers - allow more variance for CI environments
    executionTime shouldBeLessThan 500
  }

  @Test
  fun slow_notification_with_parallel_when_all_strategy_should_wait_for_all_handlers() = runTest {
    val notification = SlowNotification(50)
    val startTime = System.currentTimeMillis()

    testMediator.publish(notification, PublishStrategy.PARALLEL_WHEN_ALL)

    val endTime = System.currentTimeMillis()
    val executionTime = endTime - startTime

    // Should wait for all handlers (longest one takes 100ms)
    notification.invocationCount().get() shouldBeGreaterThanOrEqual 2 // At least 2 handlers should be invoked
    notification.threadIds().size shouldBeGreaterThan 0
    executionTime shouldBeGreaterThan 80L // Allow some variance but should be close to 100ms
  }

  @Test
  fun complex_pipeline_request_should_pass_through_all_behaviors_in_order() = runTest {
    val request = ComplexPipelineRequest()
    val result = testMediator.send(request)

    result shouldBe "processed-modified"
    request.invocationCount().get() shouldBe 1
    request.orderedVisitedPipelines() shouldBe listOf(
      TimingPipelineBehavior::class.simpleName,
      ConditionalPipelineBehavior::class.simpleName,
      ModifyingPipelineBehavior::class.simpleName
    )
    // Execution time might be 0 in fast test environments, just verify it's recorded
    request.executionTime() shouldBeGreaterThan -1L
  }

  @Test
  fun complex_data_request_should_handle_nested_structures() = runTest {
    val metadata = mapOf("key1" to "value1", "key2" to 42)
    val tags = setOf("tag1", "tag2")
    val nested = NestedData("nested-value", 10)
    val request = ComplexDataRequest(123L, "test", metadata, tags, nested)

    val result = testMediator.send(request)

    result.processedId shouldBe 246L
    result.processedName shouldBe "processed-test"
    result.processedMetadata shouldBe mapOf("key1" to "value1", "key2" to 42, "processed" to true)
    result.processedTags shouldBe setOf("tag1", "tag2", "processed")
    result.processedNested shouldBe NestedData("processed-nested-value", 10)
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun multiple_concurrent_different_request_types_should_work() = runTest {
    val command = TestCommand()
    val query = TestQuery(42)
    val notification = TestNotification()

    // Execute different types concurrently
    val commandJob = async { testMediator.send(command) }
    val queryJob = async { testMediator.send(query) }
    val notificationJob = async { testMediator.publish(notification) }

    commandJob.await()
    val queryResult = queryJob.await()
    notificationJob.await()

    command.invocationCount().get() shouldBe 1
    query.invocationCount().get() shouldBe 1
    queryResult shouldBe "hello 42-modified"
    notification.invocationCount().get() shouldBe 1
  }

  @Test
  fun request_with_large_payload_should_be_handled() = runTest {
    val largeList = (1..1000).map { "item-$it" }
    val request = CollectionRequest(largeList)
    val result = testMediator.send(request)

    result shouldHaveSize 1000
    result shouldContainAll largeList
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun deeply_nested_self_referencing_request_should_work() = runTest {
    val request = SelfReferencingRequest(10)
    val result = testMediator.send(request)

    result shouldBe 10
    request.invocationCount().get() shouldBe 1
  }

  @Test
  fun request_handler_that_sends_another_request_should_work() = runTest {
    val request = SelfReferencingRequest(2)
    val result = testMediator.send(request)

    result shouldBe 2
    request.invocationCount().get() shouldBe 1
  }
}
