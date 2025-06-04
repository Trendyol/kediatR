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
        single { TestCommandHandler(get()) }
        single { TestCommandWithResultCommandHandler(get()) } bind CommandWithResultHandler::class
        single { TestQueryHandler(get()) } bind QueryHandler::class
        single { TestNotificationHandler(get()) } bind NotificationHandler::class
        single { TestBrokenCommandHandler(get()) } bind CommandHandler::class
        single { TestPipelineCommandHandler(get()) } bind CommandHandler::class
        single { TestInheritedCommandHandlerForSpecificCommand() } bind CommandHandler::class
        single { TestCommandHandlerWithoutInjection() } bind CommandHandler::class
        single { TestCommandHandlerForTypeLimitedInheritance() } bind CommandHandler::class
        single { ParameterizedCommandHandler<String>() } bind CommandHandler::class
        single { ParameterizedCommandHandlerForInheritance<String>() } bind CommandHandler::class
        single { ParameterizedCommandWithResultHandler<Long, String>() } bind CommandWithResultHandler::class
        single { ParameterizedCommandWithResultHandlerOfInheritedHandler<String>() } bind CommandWithResultHandler::class
        single { APingHandler() } bind NotificationHandler::class
        single { AnotherPingHandler() } bind NotificationHandler::class
        single { Handler1ForNotificationOfMultipleHandlers() } bind NotificationHandler::class
        single { Handler2ForNotificationOfMultipleHandlers() } bind NotificationHandler::class
        single { InheritedNotificationHandler() } bind NotificationHandler::class
        single { ParameterizedNotificationHandler<String>() } bind NotificationHandler::class
        single { ParameterizedNotificationHandlerForInheritance<String>() } bind NotificationHandler::class
        single { TestPipelineCommandHandlerWithoutInjection() } bind CommandHandler::class
        single { TestPipelineCommandHandlerThatFails() } bind CommandHandler::class
        single { ParameterizedQueryHandler<Long, String>() } bind QueryHandler::class
        single { CommandHandlerThatPassesThroughOrderedPipelineBehaviours() } bind CommandHandler::class
        single { QueryHandlerThatPassesThroughOrderedPipelineBehaviours() } bind QueryHandler::class
        single { NotificationHandlerThatPassesThroughOrderedPipelineBehaviours() } bind NotificationHandler::class
        single { TestCommandBaseHandler() } bind CommandHandler::class
        single { TestQueryBaseHandler() } bind QueryHandler::class
        single { TestCommandWithResultBaseHandler() } bind CommandWithResultHandler::class

        // Extra
        single<MediatorAccessor> { { get<Mediator>() } }
      }
    )
  }

  private val mediator: Mediator by inject()

  override fun provideMediator(): Mediator = mediator
}
