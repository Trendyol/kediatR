package com.trendyol.kediatr.spring

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.framewokUseCases.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
  classes = [
    KediatRAutoConfiguration::class,
    TestCommandHandler::class,
    ExceptionPipelineBehavior::class,
    LoggingPipelineBehavior::class,
    TestQueryHandler::class,
    TestNotificationHandler::class,
    TestBrokenCommandHandler::class,
    TestPipelineCommandHandler::class,
    TestCommandWithResultCommandHandler::class,
    TestInheritedCommandHandlerForSpecificCommand::class
  ]
)
class MediatorTests : MediatorUseCases() {
  @Autowired
  lateinit var mediator: Mediator

  override fun provideMediator(): Mediator = mediator
}
