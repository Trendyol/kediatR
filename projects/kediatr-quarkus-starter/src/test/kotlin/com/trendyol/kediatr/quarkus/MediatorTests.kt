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
  fun handler1(mediator: Mediator) = TestRequestHandler({ mediator })

  @Produces
  fun handler2(mediator: Mediator) = TestCommandWithResultRequestHandler({ mediator })

  @Produces
  fun handler3(mediator: Mediator) = TestBrokenRequestHandler({ mediator })

  @Produces
  fun handler4(mediator: Mediator) = TestPipelineRequestHandler({ mediator })

  @Produces
  fun handler5() = TestInheritedRequestHandlerForSpecificCommand()

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
  fun handler6() = TestRequestHandlerWithoutInjection()

  @Produces
  fun handler7() = TestRequestHandlerForTypeLimitedInheritance()

  @Produces
  fun <T> handler8(): ParameterizedRequestHandler<T> = ParameterizedRequestHandler()

  @Produces
  fun <T> handler9() = ParameterizedRequestHandlerForInheritance<T>()

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
  fun handler16Second() = InheritedNotificationHandler2()

  @Produces
  fun <T> handler17() = ParameterizedNotificationHandler<T>()

  @Produces
  fun <T> handler18() = ParameterizedNotificationHandlerForInheritance<T>()

  @Produces
  fun handler19() = TestPipelineRequestHandlerWithoutInjection()

  @Produces
  fun handler20() = TestPipelineRequestHandlerThatFails()

  @Produces
  fun <T, R> handler22() = ParameterizedQueryHandler<T, R>()

  @Produces
  fun pipeline4() = FirstPipelineBehaviour()

  @Produces
  fun pipeline5() = SecondPipelineBehaviour()

  @Produces
  fun pipeline6() = ThirdPipelineBehaviour()

  @Produces
  fun handler23() = RequestHandlerThatPassesThroughOrderedPipelineBehaviours()

  @Produces
  fun handler24() = QueryHandlerThatPassesThroughOrderedPipelineBehaviours()

  @Produces
  fun handler25() = NotificationHandlerThatPassesThroughOrderedPipelineBehaviours()

  @Produces
  fun handler26() = TestCommandBaseHandler()

  @Produces
  fun handler27() = TestQueryBaseHandler()

  @Produces
  fun handler28() = TestCommandWithResultBaseHandler()

  @Produces
  fun handler29() = TestCommandForInheritanceWithFallbackHandlerHandler()

  @Produces
  fun handler30() = TestRequestHandlerForCommandInherited2()

  @Produces
  fun pipelineModifying() = ModifyingPipelineBehavior()

  @Produces
  fun pipelineTiming() = TimingPipelineBehavior()

  @Produces
  fun pipelineConditional() = ConditionalPipelineBehavior()

  @Produces
  fun handlerNullableResult() = RequestWithNullableResultHandler()

  @Produces
  fun handlerNullParameter() = RequestWithNullParameterHandler()

  @Produces
  fun <T, R> handlerNestedGeneric() = NestedGenericRequestHandler<T, R>()

  @Produces
  fun <T> handlerWildcardGeneric() = WildcardGenericRequestHandler<T>()

  @Produces
  fun handlerConcurrent() = ConcurrentRequestHandler()

  @Produces
  fun handlerLongRunning() = LongRunningRequestHandler()

  @Produces
  fun handlerMultiInterface() = MultiInterfaceRequestHandler()

  @Produces
  fun handlerEmpty() = EmptyRequestHandler()

  @Produces
  fun handlerVoidResult() = VoidResultRequestHandler()

  @Produces
  fun handlerCollection() = CollectionRequestHandler()

  @Produces
  fun handlerSpecificException() = RequestThatThrowsSpecificExceptionHandler()

  @Produces
  fun handlerRuntimeException() = RequestThatThrowsRuntimeExceptionHandler()

  @Produces
  fun handlerComplexPipeline() = ComplexPipelineRequestHandler()

  @Produces
  fun handlerComplexData() = ComplexDataRequestHandler()

  @Produces
  fun notificationHandlerException1() = NotificationThatThrowsExceptionHandler1()

  @Produces
  fun notificationHandlerException2() = NotificationThatThrowsExceptionHandler2()

  @Produces
  fun notificationHandlerException3() = NotificationThatThrowsExceptionHandler3()

  @Produces
  fun notificationHandlerSlow1() = SlowNotificationHandler1()

  @Produces
  fun notificationHandlerSlow2() = SlowNotificationHandler2()

  @Produces
  fun notificationHandlerSlow3() = SlowNotificationHandler3()

  @Produces
  fun selfReferencingRequestHandler(mediator: Mediator) = SelfReferencingRequestHandler { mediator }
}
