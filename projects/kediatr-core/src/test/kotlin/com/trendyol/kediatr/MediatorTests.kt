package com.trendyol.kediatr

import com.trendyol.kediatr.HandlerRegistryProvider.Companion.createMediator
import com.trendyol.kediatr.testing.*

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
      TestRequestHandlerForCommandInherited2()
    )
  )
}
