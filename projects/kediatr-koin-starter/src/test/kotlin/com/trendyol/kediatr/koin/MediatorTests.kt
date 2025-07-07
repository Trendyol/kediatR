package com.trendyol.kediatr.koin

import com.trendyol.kediatr.*
import com.trendyol.kediatr.testing.*
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.*
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

        // Handlers
        single { TestRequestHandler(get()) }
        single { TestCommandWithResultRequestHandler(get()) } bind RequestHandler::class
        single { TestQueryHandler(get()) } bind RequestHandler::class
        single { TestNotificationHandler(get()) } bind NotificationHandler::class
        single { TestBrokenRequestHandler(get()) } bind RequestHandler::class
        single { TestPipelineRequestHandler(get()) } bind RequestHandler::class
        single { TestInheritedRequestHandlerForSpecificCommand() } bind RequestHandler::class
        single { TestRequestHandlerWithoutInjection() } bind RequestHandler::class
        single { TestRequestHandlerForTypeLimitedInheritance() } bind RequestHandler::class
        single { ParameterizedRequestHandler<String>() } bind RequestHandler::class
        single { ParameterizedRequestHandlerForInheritance<String>() } bind RequestHandler::class
        single { ParameterizedCommandWithResultHandler<Long, String>() } bind RequestHandler::class
        single { ParameterizedCommandWithResultHandlerOfInheritedHandler<String>() } bind RequestHandler::class
        single { APingHandler() } bind NotificationHandler::class
        single { AnotherPingHandler() } bind NotificationHandler::class
        single { Handler1ForNotificationOfMultipleHandlers() } bind NotificationHandler::class
        single { Handler2ForNotificationOfMultipleHandlers() } bind NotificationHandler::class
        single { InheritedNotificationHandler() } bind NotificationHandler::class
        single { ParameterizedNotificationHandler<String>() } bind NotificationHandler::class
        single { ParameterizedNotificationHandlerForInheritance<String>() } bind NotificationHandler::class
        single { TestPipelineRequestHandlerWithoutInjection() } bind RequestHandler::class
        single { TestPipelineRequestHandlerThatFails() } bind RequestHandler::class
        single { ParameterizedQueryHandler<Long, String>() } bind RequestHandler::class
        single { RequestHandlerThatPassesThroughOrderedPipelineBehaviours() } bind RequestHandler::class
        single { QueryHandlerThatPassesThroughOrderedPipelineBehaviours() } bind RequestHandler::class
        single { NotificationHandlerThatPassesThroughOrderedPipelineBehaviours() } bind NotificationHandler::class
        single { TestCommandBaseHandler() } bind RequestHandler::class
        single { TestQueryBaseHandler() } bind RequestHandler::class
        single { TestCommandWithResultBaseHandler() } bind RequestHandler::class
        single { TestCommandForInheritanceWithFallbackHandlerHandler() } bind RequestHandler::class
        single { TestRequestHandlerForCommandInherited2() } bind RequestHandler::class

        // Extra
        single<MediatorAccessor> { { get<Mediator>() } }
      }
    )
  }

  private val mediator: Mediator by inject()

  override fun provideMediator(): Mediator = mediator
}
