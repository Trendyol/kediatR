package com.trendyol.kediatr.coreUseCases

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.NotificationHandler
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import java.util.concurrent.CountDownLatch

private var asyncCountDownLatch = CountDownLatch(1)

abstract class NotificationHandlerUseCases : MediatorTestConvention() {
  @BeforeEach
  fun beforeEach() {
    asyncCountDownLatch = CountDownLatch(1)
  }

  @Test
  fun `async notification handler should be called`() = runTest {
    val pingHandler = APingHandler()
    val anotherPingHandler = AnotherPingHandler()
    val bus: Mediator = newMediator(handlers = listOf(pingHandler, anotherPingHandler))
    bus.publish(ExtendedPing())

    asyncCountDownLatch.count shouldBe 0
  }

  @Test
  fun multiple_handlers_for_a_notification_should_be_dispatched() = runTest {
    var invocationCount = 0

    class MyNotification : Notification

    class Handler1 : NotificationHandler<MyNotification> {
      override suspend fun handle(notification: MyNotification) {
        invocationCount++
      }
    }

    class Handler2 : NotificationHandler<MyNotification> {
      override suspend fun handle(notification: MyNotification) {
        invocationCount++
      }
    }

    val bus: Mediator = newMediator(handlers = listOf(Handler1(), Handler2()))
    bus.publish(MyNotification())
    invocationCount shouldBe 2
  }

  @Test
  fun inherited_notification_handler_should_be_called() = runTest {
    class PingForInherited : Notification

    abstract class NotificationHandlerBase<TNotification : Notification> : NotificationHandler<TNotification>

    class InheritedNotificationHandler : NotificationHandlerBase<PingForInherited>() {
      override suspend fun handle(notification: PingForInherited) {
        asyncCountDownLatch.countDown()
      }
    }

    val bus: Mediator = newMediator(handlers = listOf(InheritedNotificationHandler()))
    bus.publish(PingForInherited())

    asyncCountDownLatch.count shouldBe 0
  }

  inner class ParameterizedNotification<T>(val param: T) : Notification

  inner class ParameterizedNotificationHandler<A> : NotificationHandler<ParameterizedNotification<A>> {
    override suspend fun handle(notification: ParameterizedNotification<A>) {
      notification.param shouldBe "MyParam"
      asyncCountDownLatch.countDown()
    }
  }

  @Test
  fun `async notification should be fired`() = runTest {
    // given
    val handler = ParameterizedNotificationHandler<ParameterizedNotification<String>>()
    val bus: Mediator = newMediator(handlers = listOf(handler))

    // when
    bus.publish(ParameterizedNotification("MyParam"))

    // then
    asyncCountDownLatch.count shouldBe 0
  }

  @Test
  fun inherited_notification_handler_should_be_called_with_param() = runTest {
    var invocationCount = 0
    var parameter = ""

    class ParameterizedNotification<T>(val param: T) : Notification

    abstract class NotificationHandlerBase<TNotification : Notification> : NotificationHandler<TNotification>

    class ParameterizedNotificationHandler<A> : NotificationHandlerBase<ParameterizedNotification<A>>() {
      override suspend fun handle(notification: ParameterizedNotification<A>) {
        parameter = notification.param.toString()
        invocationCount++
      }
    }

    val nHandler = ParameterizedNotificationHandler<ParameterizedNotification<String>>()
    val bus: Mediator = newMediator(handlers = listOf(nHandler))
    bus.publish(ParameterizedNotification("invoked"))

    invocationCount shouldBe 1
    parameter shouldBe "invoked"
  }
}

private open class Ping : Notification

private class ExtendedPing : Ping()

private class APingHandler : NotificationHandler<ExtendedPing> {
  override suspend fun handle(notification: ExtendedPing) {
    asyncCountDownLatch.countDown()
  }
}

private class AnotherPingHandler : NotificationHandler<Ping> {
  override suspend fun handle(notification: Ping) {
    asyncCountDownLatch.countDown()
  }
}
