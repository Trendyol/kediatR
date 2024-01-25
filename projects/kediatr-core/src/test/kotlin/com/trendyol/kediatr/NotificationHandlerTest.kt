package com.trendyol.kediatr

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private var asyncCountDownLatch = CountDownLatch(1)

private open class Ping : Notification

private class ExtendedPing : Ping()

private class AnPingHandler : NotificationHandler<ExtendedPing> {
    override suspend fun handle(notification: ExtendedPing) {
        asyncCountDownLatch.countDown()
    }
}

private class AnotherPingHandler : NotificationHandler<Ping> {
    override suspend fun handle(notification: Ping) {
        asyncCountDownLatch.countDown()
    }
}

class NotificationHandlerTest {
    init {
        asyncCountDownLatch = CountDownLatch(1)
    }

    @Test
    fun `async notification handler should be called`() =
        runBlocking {
            val pingHandler = AnPingHandler()
            val anotherPingHandler = AnotherPingHandler()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(
                    Pair(AnPingHandler::class.java, pingHandler),
                    Pair(AnotherPingHandler::class.java, anotherPingHandler)
                )
            val provider = ManualDependencyProvider(handlers)
            val bus: Mediator = MediatorBuilder(provider).build()
            bus.publish(ExtendedPing())

            assertTrue {
                asyncCountDownLatch.count == 0L
            }
        }

    @Test
    fun multiple_handlers_for_a_notification_should_be_dispatched() =
        runBlocking {
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

            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(
                    Pair(Handler1::class.java, Handler1()),
                    Pair(Handler2::class.java, Handler2())
                )
            val provider = ManualDependencyProvider(handlers)
            val bus: Mediator = MediatorBuilder(provider).build()
            bus.publish(MyNotification())
            assertEquals(2, invocationCount)
        }

    @Test
    fun inherited_notification_handler_should_be_called() =
        runBlocking {
            class PingForInherited : Notification

            abstract class NotificationHandlerBase<TNotification : Notification> : NotificationHandler<TNotification>

            class InheritedNotificationHandler : NotificationHandlerBase<PingForInherited>() {
                override suspend fun handle(notification: PingForInherited) {
                    asyncCountDownLatch.countDown()
                }
            }

            val nHandler = InheritedNotificationHandler()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(InheritedNotificationHandler::class.java, nHandler))
            val provider = ManualDependencyProvider(handlers)
            val bus: Mediator = MediatorBuilder(provider).build()
            bus.publish(PingForInherited())

            assertEquals(0, asyncCountDownLatch.count)
        }

    @Nested
    inner class ParamaterizedTests {
        inner class ParameterizedNotification<T>(val param: T) : Notification

        inner class ParameterizedNotificationHandler<A> : NotificationHandler<ParameterizedNotification<A>> {
            override suspend fun handle(notification: ParameterizedNotification<A>) {
                asyncCountDownLatch.countDown()
            }
        }

        @Test
        fun `async notification should be fired`() =
            runBlocking {
                // given
                val handler = ParameterizedNotificationHandler<ParameterizedNotification<String>>()
                val handlers: HashMap<Class<*>, Any> =
                    hashMapOf(Pair(ParameterizedNotificationHandler::class.java, handler))
                val provider = ManualDependencyProvider(handlers)
                val bus: Mediator = MediatorBuilder(provider).build()

                // when
                bus.publish(ParameterizedNotification("MyParam"))

                // then
                assertTrue {
                    asyncCountDownLatch.count == 0L
                }
            }

        @Test
        fun inherited_notification_handler_should_be_called() =
            runBlocking {
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
                val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedNotificationHandler::class.java, nHandler))
                val provider = ManualDependencyProvider(handlers)
                val bus: Mediator = MediatorBuilder(provider).build()
                bus.publish(ParameterizedNotification("invoked"))

                assertEquals(1, invocationCount)
                assertEquals("invoked", parameter)
            }
    }
}
