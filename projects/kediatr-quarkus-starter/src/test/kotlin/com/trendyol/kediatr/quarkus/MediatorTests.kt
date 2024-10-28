package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.framewokUseCases.*
import io.quarkus.test.junit.QuarkusTest
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject

@QuarkusTest
class MediatorTests : MediatorUseCases() {
  @Inject
  lateinit var mediator: Mediator

  override fun provideMediator(): Mediator = mediator

  @Produces
  fun handler1(mediator: Mediator) = TestCommandHandler(mediator)

  @Produces
  fun handler2(mediator: Mediator) = TestCommandWithResultCommandHandler(mediator)

  @Produces
  fun handler3(mediator: Mediator): TestBrokenCommandHandler = TestBrokenCommandHandler(mediator)

  @Produces
  fun handler4(mediator: Mediator): TestPipelineCommandHandler = TestPipelineCommandHandler(mediator)

  @Produces
  fun handler5(): TestInheritedCommandHandlerForSpecificCommand =
    TestInheritedCommandHandlerForSpecificCommand()

  @Produces
  fun notificationHandler(mediator: Mediator) = TestNotificationHandler(mediator)

  @Produces
  fun pipeline1() = ExceptionPipelineBehavior()

  @Produces
  fun pipeline2() = LoggingPipelineBehavior()

  @Produces
  fun provideQueryHandler(mediator: Mediator) = TestQueryHandler(mediator)
}
