package com.trendyol.kediatr

import com.trendyol.kediatr.HandlerRegistryProvider.Companion.createMediator
import com.trendyol.kediatr.testing.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MediatorTests : MediatorUseCases() {
  override fun provideMediator(): Mediator = createMediator(
    handlers = listOf(
      TestRequestHandler(mediator = { testMediator }),
      TestPipelineRequestHandler(mediator = { testMediator }),
      TestCommandWithResultRequestHandler(mediator = { testMediator }),
      TestNotificationHandler(mediator = { testMediator }),
      TestQueryHandler(mediator = { testMediator }),
      TestRequestHandlerWithoutInjection(),
      TestInheritedRequestHandlerForSpecificCommand(),
      TestRequestHandlerForTypeLimitedInheritance(),
      ParameterizedRequestHandler<String>(),
      ParameterizedRequestHandlerForInheritance<String>(),
      ParameterizedCommandWithResultHandler<Long, String>(),
      ParameterizedCommandWithResultHandlerOfInheritedHandler<String>(),
      APingHandler(),
      AnotherPingHandler(),
      Handler1ForNotificationOfMultipleHandlers(),
      Handler2ForNotificationOfMultipleHandlers(),
      InheritedNotificationHandler(),
      InheritedNotificationHandler2(),
      ParameterizedNotificationHandler<String>(),
      ParameterizedNotificationHandlerForInheritance<String>(),
      TestPipelineRequestHandlerWithoutInjection(),
      TestPipelineRequestHandlerThatFails(),
      ExceptionPipelineBehavior(),
      LoggingPipelineBehavior(),
      InheritedPipelineBehaviour(),
      ParameterizedQueryHandler<Long, String>(),
      FirstPipelineBehaviour(),
      SecondPipelineBehaviour(),
      ThirdPipelineBehaviour(),
      RequestHandlerThatPassesThroughOrderedPipelineBehaviours(),
      QueryHandlerThatPassesThroughOrderedPipelineBehaviours(),
      NotificationHandlerThatPassesThroughOrderedPipelineBehaviours(),
      TestCommandBaseHandler(),
      TestQueryBaseHandler(),
      TestCommandWithResultBaseHandler(),
      TestCommandForInheritanceWithFallbackHandlerHandler(),
      TestRequestHandlerForCommandInherited2(),
      TestBrokenRequestHandler(mediator = { testMediator }),
      RequestWithNullableResultHandler(),
      RequestWithNullParameterHandler(),
      NestedGenericRequestHandler<String, Int>(),
      WildcardGenericRequestHandler<Int>(),
      WildcardGenericRequestHandler<Double>(),
      ConcurrentRequestHandler(),
      LongRunningRequestHandler(),
      SelfReferencingRequestHandler(mediator = { testMediator }),
      MultiInterfaceRequestHandler(),
      EmptyRequestHandler(),
      VoidResultRequestHandler(),
      CollectionRequestHandler(),
      RequestThatThrowsSpecificExceptionHandler(),
      RequestThatThrowsRuntimeExceptionHandler(),
      NotificationThatThrowsExceptionHandler1(),
      NotificationThatThrowsExceptionHandler2(),
      NotificationThatThrowsExceptionHandler3(),
      SlowNotificationHandler1(),
      SlowNotificationHandler2(),
      SlowNotificationHandler3(),
      ModifyingPipelineBehavior(),
      ConditionalPipelineBehavior(),
      TimingPipelineBehavior(),
      ComplexPipelineRequestHandler(),
      ComplexDataRequestHandler()
    )
  )

  @Test
  fun command_with_multiple_handlers_should_fail() = runTest {
    // Arrange - Test that when multiple handlers are registered for the same command type,
    val handlers = listOf(
      FirstHandlerForCommand(),
      SecondHandlerForCommand()
    )

    // Act
    val exception = shouldThrow<IllegalStateException> {
      createMediator(handlers)
    }

    // Assert
    exception.message shouldBe "Multiple handlers registered for request type: com.trendyol.kediatr.testing.CommandWithMultipleHandlers\n" +
      "Existing handler: com.trendyol.kediatr.testing.FirstHandlerForCommand\n" +
      "Duplicate handler: com.trendyol.kediatr.testing.SecondHandlerForCommand"
  }
}
