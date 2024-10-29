package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.testing.*
import io.quarkus.test.junit.QuarkusTest
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject

@QuarkusTest
class MediatorTests : MediatorUseCases() {
  @Inject
  lateinit var mediator: Mediator

  override fun provideMediator(): Mediator = mediator

  @Produces
  fun handler1(mediator: Mediator) = TestCommandHandler({ mediator })

  @Produces
  fun handler2(mediator: Mediator) = TestCommandWithResultCommandHandler({ mediator })

  @Produces
  fun handler3(mediator: Mediator) = TestBrokenCommandHandler({ mediator })

  @Produces
  fun handler4(mediator: Mediator) = TestPipelineCommandHandler({ mediator })

  @Produces
  fun handler5() = TestInheritedCommandHandlerForSpecificCommand()

  @Produces
  fun notificationHandler(mediator: Mediator) = TestNotificationHandler({ mediator })

  @Produces
  fun pipeline1() = ExceptionPipelineBehavior()

  @Produces
  fun pipeline2() = LoggingPipelineBehavior()

  @Produces
  fun pipeline3() = InheritedPipelineBehaviour()

  @Produces
  fun queryHandler(mediator: Mediator) = TestQueryHandler({ mediator })

  @Produces
  fun handler6() = TestCommandHandlerWithoutInjection()

  @Produces
  fun handler7() = TestCommandHandlerForTypeLimitedInheritance()

  @Produces
  fun <T> handler8(): ParameterizedCommandHandler<T> = ParameterizedCommandHandler()

  @Produces
  fun <T> handler9() = ParameterizedCommandHandlerForInheritance<T>()

  @Produces
  fun <T, R> handler10() = ParameterizedCommandWithResultHandler<T, R>()

  @Produces
  fun <T> handler11() = ParameterizedCommandWithResultHandlerOfInheritedHandler<T>()

  @Produces
  fun handler12() = APingHandler()

  @Produces
  fun handler13() = AnotherPingHandler()

  @Produces
  fun handler14() = Handler1ForNotificationOfMultipleHandlers()

  @Produces
  fun handler15() = Handler2ForNotificationOfMultipleHandlers()

  @Produces
  fun handler16() = InheritedNotificationHandler()

  @Produces
  fun <T> handler17() = ParameterizedNotificationHandler<T>()

  @Produces
  fun <T> handler18() = ParameterizedNotificationHandlerForInheritance<T>()

  @Produces
  fun handler19() = TestPipelineCommandHandlerWithoutInjection()

  @Produces
  fun handler20() = TestPipelineCommandHandlerThatFails()

  @Produces
  fun <T, R> handler22() = ParameterizedQueryHandler<T, R>()

  @Produces
  fun pipeline4() = FirstPipelineBehaviour()

  @Produces
  fun pipeline5() = SecondPipelineBehaviour()

  @Produces
  fun pipeline6() = ThirdPipelineBehaviour()

  @Produces
  fun handler23() = CommandHandlerThatPassesThroughOrderedPipelineBehaviours()

  @Produces
  fun handler24() = QueryHandlerThatPassesThroughOrderedPipelineBehaviours()

  @Produces
  fun handler25() = NotificationHandlerThatPassesThroughOrderedPipelineBehaviours()
}
