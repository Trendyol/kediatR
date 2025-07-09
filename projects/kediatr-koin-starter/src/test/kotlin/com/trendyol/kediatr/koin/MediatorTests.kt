package com.trendyol.kediatr.koin

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.testing.*
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.*
import org.koin.test.junit5.KoinTestExtension

class MediatorTests :
  MediatorUseCases(),
  KoinTest {
  @JvmField
  @RegisterExtension
  val koinTestExtension = KoinTestExtension.create {
    modules(
      module {
        single { KediatRKoin.getMediator() }

        // Pipeline behaviours
        single { InheritedPipelineBehaviour() }
        single { ExceptionPipelineBehavior() }
        single { LoggingPipelineBehavior() }
        single { FirstPipelineBehaviour() }
        single { SecondPipelineBehaviour() }
        single { ThirdPipelineBehaviour() }
        single { ModifyingPipelineBehavior() }
        single { TimingPipelineBehavior() }
        single { ConditionalPipelineBehavior() }

        // Handlers
        single { TestRequestHandler(get()) }
        single { TestCommandWithResultRequestHandler(get()) }
        single { TestQueryHandler(get()) }
        single { TestNotificationHandler(get()) }
        single { TestBrokenRequestHandler(get()) }
        single { TestPipelineRequestHandler(get()) }
        single { TestInheritedRequestHandlerForSpecificCommand() }
        single { TestRequestHandlerWithoutInjection() }
        single { TestRequestHandlerForTypeLimitedInheritance() }
        single { ParameterizedRequestHandler<Any>() }
        single { ParameterizedRequestHandlerForInheritance<Any>() }
        single { ParameterizedCommandWithResultHandler<Any, Any>() }
        single { ParameterizedCommandWithResultHandlerOfInheritedHandler<Any>() }
        single { APingHandler() }
        single { AnotherPingHandler() }
        single { Handler1ForNotificationOfMultipleHandlers() }
        single { Handler2ForNotificationOfMultipleHandlers() }
        single { InheritedNotificationHandler() }
        single { InheritedNotificationHandler2() }
        single { ParameterizedNotificationHandler<Any>() }
        single { ParameterizedNotificationHandlerForInheritance<Any>() }
        single { TestPipelineRequestHandlerWithoutInjection() }
        single { TestPipelineRequestHandlerThatFails() }
        single { ParameterizedQueryHandler<Any, Any>() }
        single { RequestHandlerThatPassesThroughOrderedPipelineBehaviours() }
        single { QueryHandlerThatPassesThroughOrderedPipelineBehaviours() }
        single { NotificationHandlerThatPassesThroughOrderedPipelineBehaviours() }
        single { TestCommandBaseHandler() }
        single { TestQueryBaseHandler() }
        single { TestCommandWithResultBaseHandler() }
        single { TestCommandForInheritanceWithFallbackHandlerHandler() }
        single { TestRequestHandlerForCommandInherited2() }

        // New Edge Case Handlers (that actually exist)
        single { RequestWithNullableResultHandler() }
        single { RequestWithNullParameterHandler() }
        single { NestedGenericRequestHandler<Any, Any>() }
        single { WildcardGenericRequestHandler<Any>() }
        single { ConcurrentRequestHandler() }
        single { LongRunningRequestHandler() }
        single { MultiInterfaceRequestHandler() }
        single { EmptyRequestHandler() }
        single { VoidResultRequestHandler() }
        single { CollectionRequestHandler() }
        single { RequestThatThrowsSpecificExceptionHandler() }
        single { RequestThatThrowsRuntimeExceptionHandler() }
        single { ComplexPipelineRequestHandler() }
        single { ComplexDataRequestHandler() }

        // New Edge Case Notification Handlers (that actually exist)
        single { NotificationThatThrowsExceptionHandler1() }
        single { NotificationThatThrowsExceptionHandler2() }
        single { NotificationThatThrowsExceptionHandler3() }
        single { SlowNotificationHandler1() }
        single { SlowNotificationHandler2() }
        single { SlowNotificationHandler3() }
        single { SelfReferencingRequestHandler(get()) }

        // Extra
        single<MediatorAccessor> { { get<Mediator>() } }
      }
    )
  }

  private val mediator: Mediator by inject()

  override fun provideMediator(): Mediator = mediator
}
