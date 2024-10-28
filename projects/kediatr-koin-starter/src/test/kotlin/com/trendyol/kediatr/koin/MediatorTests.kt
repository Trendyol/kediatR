package com.trendyol.kediatr.koin

import com.trendyol.kediatr.*
import com.trendyol.kediatr.framewokUseCases.*
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.*
import org.koin.test.*
import org.koin.test.junit5.KoinTestExtension

class MediatorTests : KoinTest, MediatorUseCases() {
  @JvmField
  @RegisterExtension
  val koinTestExtension = KoinTestExtension.create {
    modules(
      module {
        single { KediatRKoin.getMediator() }
        single { ExceptionPipelineBehavior() }
        single { LoggingPipelineBehavior() }
        single { TestCommandHandler(get()) }
        single { TestCommandWithResultCommandHandler(get()) } bind CommandWithResultHandler::class
        single { TestQueryHandler(get()) } bind QueryHandler::class
        single { TestNotificationHandler(get()) } bind NotificationHandler::class
        single { TestBrokenCommandHandler(get()) } bind CommandHandler::class
        single { TestPipelineCommandHandler(get()) } bind CommandHandler::class
      }
    )
  }

  private val mediator: Mediator by inject()

  override fun provideMediator(): Mediator = mediator
}
