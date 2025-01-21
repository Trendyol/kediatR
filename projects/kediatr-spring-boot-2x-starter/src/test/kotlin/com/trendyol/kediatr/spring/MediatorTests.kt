package com.trendyol.kediatr.spring

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.testing.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.*

@SpringBootTest(
  classes = [
    KediatRAutoConfiguration::class,
    MediatorTests.TestConfiguration::class,
    TestCommandHandler::class,
    ExceptionPipelineBehavior::class,
    LoggingPipelineBehavior::class,
    TestQueryHandler::class,
    TestNotificationHandler::class,
    TestBrokenCommandHandler::class,
    TestPipelineCommandHandler::class,
    TestCommandWithResultCommandHandler::class,
    TestInheritedCommandHandlerForSpecificCommand::class,
    TestCommandHandlerWithoutInjection::class,
    TestCommandHandlerForTypeLimitedInheritance::class,
    ParameterizedCommandHandler::class,
    ParameterizedCommandHandlerForInheritance::class,
    ParameterizedCommandWithResultHandler::class,
    ParameterizedCommandWithResultHandlerOfInheritedHandler::class,
    APingHandler::class,
    AnotherPingHandler::class,
    Handler1ForNotificationOfMultipleHandlers::class,
    Handler2ForNotificationOfMultipleHandlers::class,
    InheritedNotificationHandler::class,
    ParameterizedNotificationHandler::class,
    ParameterizedNotificationHandlerForInheritance::class,
    TestPipelineCommandHandlerWithoutInjection::class,
    TestPipelineCommandHandlerThatFails::class,
    InheritedPipelineBehaviour::class,
    ParameterizedQueryHandler::class,
    FirstPipelineBehaviour::class,
    SecondPipelineBehaviour::class,
    ThirdPipelineBehaviour::class,
    CommandHandlerThatPassesThroughOrderedPipelineBehaviours::class,
    QueryHandlerThatPassesThroughOrderedPipelineBehaviours::class,
    NotificationHandlerThatPassesThroughOrderedPipelineBehaviours::class
  ]
)
class MediatorTests : MediatorUseCases() {
  @Autowired
  lateinit var mediator: Mediator

  override fun provideMediator(): Mediator = mediator

  @Configuration
  open class TestConfiguration {
    @Bean
    open fun mediatorAccessor(mediator: Mediator): MediatorAccessor = { mediator }
  }
}
