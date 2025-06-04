package com.trendyol.kediatr

import com.trendyol.kediatr.MappingDependencyProvider.Companion.createMediator
import com.trendyol.kediatr.testing.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MediatorTests : MediatorUseCases() {
  override fun provideMediator(): Mediator = createMediator(
    handlers = listOf(
      TestCommandHandler(mediator = { testMediator }),
      TestPipelineCommandHandler(mediator = { testMediator }),
      TestCommandWithResultCommandHandler(mediator = { testMediator }),
      TestNotificationHandler(mediator = { testMediator }),
      TestQueryHandler(mediator = { testMediator }),
      TestCommandHandlerWithoutInjection(),
      TestInheritedCommandHandlerForSpecificCommand(),
      TestCommandHandlerForTypeLimitedInheritance(),
      ParameterizedCommandHandler<String>(),
      ParameterizedCommandHandlerForInheritance<String>(),
      ParameterizedCommandWithResultHandler<Long, String>(),
      ParameterizedCommandWithResultHandlerOfInheritedHandler<String>(),
      APingHandler(),
      AnotherPingHandler(),
      Handler1ForNotificationOfMultipleHandlers(),
      Handler2ForNotificationOfMultipleHandlers(),
      InheritedNotificationHandler(),
      ParameterizedNotificationHandler<String>(),
      ParameterizedNotificationHandlerForInheritance<String>(),
      TestPipelineCommandHandlerWithoutInjection(),
      TestPipelineCommandHandlerThatFails(),
      ExceptionPipelineBehavior(),
      LoggingPipelineBehavior(),
      InheritedPipelineBehaviour(),
      ParameterizedQueryHandler<Long, String>(),
      FirstPipelineBehaviour(),
      SecondPipelineBehaviour(),
      ThirdPipelineBehaviour(),
      CommandHandlerThatPassesThroughOrderedPipelineBehaviours(),
      QueryHandlerThatPassesThroughOrderedPipelineBehaviours(),
      NotificationHandlerThatPassesThroughOrderedPipelineBehaviours(),
      TestCommandBaseHandler(),
      TestQueryBaseHandler(),
      TestCommandWithResultBaseHandler(),
      TestCommandForInheritanceWithFallbackHandlerHandler(),
      TestCommandHandlerForCommandInherited2()
    )
  )

  @Test
  fun `when a publish strategy is defined it should be set`() {
    listOf(
      ContinueOnExceptionPublishStrategy(),
      ParallelNoWaitPublishStrategy(),
      ParallelWhenAllPublishStrategy(),
      StopOnExceptionPublishStrategy()
    ).forEach {
      val builder = MediatorBuilder(MappingDependencyProvider(hashMapOf())).withPublishStrategy(it)
      builder.defaultPublishStrategy shouldBe it
    }
  }
}
