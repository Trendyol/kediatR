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
    TestRequestHandler::class,
    ExceptionPipelineBehavior::class,
    LoggingPipelineBehavior::class,
    TestQueryHandler::class,
    TestNotificationHandler::class,
    TestBrokenRequestHandler::class,
    TestPipelineRequestHandler::class,
    TestCommandWithResultRequestHandler::class,
    TestInheritedRequestHandlerForSpecificCommand::class,
    TestRequestHandlerWithoutInjection::class,
    TestRequestHandlerForTypeLimitedInheritance::class,
    ParameterizedRequestHandler::class,
    ParameterizedRequestHandlerForInheritance::class,
    ParameterizedCommandWithResultHandler::class,
    ParameterizedCommandWithResultHandlerOfInheritedHandler::class,
    APingHandler::class,
    AnotherPingHandler::class,
    Handler1ForNotificationOfMultipleHandlers::class,
    Handler2ForNotificationOfMultipleHandlers::class,
    InheritedNotificationHandler::class,
    InheritedNotificationHandler2::class,
    ParameterizedNotificationHandler::class,
    ParameterizedNotificationHandlerForInheritance::class,
    TestPipelineRequestHandlerWithoutInjection::class,
    TestPipelineRequestHandlerThatFails::class,
    InheritedPipelineBehaviour::class,
    ParameterizedQueryHandler::class,
    FirstPipelineBehaviour::class,
    SecondPipelineBehaviour::class,
    ThirdPipelineBehaviour::class,
    RequestHandlerThatPassesThroughOrderedPipelineBehaviours::class,
    QueryHandlerThatPassesThroughOrderedPipelineBehaviours::class,
    NotificationHandlerThatPassesThroughOrderedPipelineBehaviours::class,
    TestCommandBaseHandler::class,
    TestQueryBaseHandler::class,
    TestCommandWithResultBaseHandler::class,
    TestCommandForInheritanceWithFallbackHandlerHandler::class,
    TestRequestHandlerForCommandInherited2::class,
    ModifyingPipelineBehavior::class,
    TimingPipelineBehavior::class,
    ConditionalPipelineBehavior::class,
    RequestWithNullableResultHandler::class,
    RequestWithNullParameterHandler::class,
    NestedGenericRequestHandler::class,
    WildcardGenericRequestHandler::class,
    ConcurrentRequestHandler::class,
    LongRunningRequestHandler::class,
    MultiInterfaceRequestHandler::class,
    EmptyRequestHandler::class,
    VoidResultRequestHandler::class,
    CollectionRequestHandler::class,
    RequestThatThrowsSpecificExceptionHandler::class,
    RequestThatThrowsRuntimeExceptionHandler::class,
    ComplexPipelineRequestHandler::class,
    ComplexDataRequestHandler::class,
    NotificationThatThrowsExceptionHandler1::class,
    NotificationThatThrowsExceptionHandler2::class,
    NotificationThatThrowsExceptionHandler3::class,
    SlowNotificationHandler1::class,
    SlowNotificationHandler2::class,
    SlowNotificationHandler3::class,
    SelfReferencingRequestHandler::class,
    CatchAllNotificationsHandler::class
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
