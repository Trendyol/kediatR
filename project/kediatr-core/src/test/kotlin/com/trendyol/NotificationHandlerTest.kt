package com.trendyol

import com.trendyol.kediatr.AsyncNotificationHandler
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.NotificationHandler
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private var asyncCountDownLatch = CountDownLatch(1)
private var countDownLatch = CountDownLatch(1)

private open class Ping : Notification
private class ExtendedPing : Ping()

private class AnAsyncPingHandler : AsyncNotificationHandler<ExtendedPing> {
    override suspend fun handle(notification: ExtendedPing) {
        asyncCountDownLatch.countDown()
    }
}

private class AnotherAsyncPingHandler : AsyncNotificationHandler<Ping> {
    override suspend fun handle(notification: Ping) {
        asyncCountDownLatch.countDown()
    }
}

private class PingHandler : NotificationHandler<Ping> {
    override fun handle(notification: Ping) {
        countDownLatch.countDown()
    }
}

class NotificationHandlerTest {
    init {
        asyncCountDownLatch = CountDownLatch(1)
    }

    @Test
    fun `notification handler should be called`() {
        val pingHandler = PingHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(PingHandler::class.java, pingHandler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.publishNotification(Ping())

        assertTrue {
            countDownLatch.count == 0L
        }
    }

    @Test
    fun `async notification handler should be called`() = runBlocking {
        val pingHandler = AnAsyncPingHandler()
        val anotherPingHandler = AnotherAsyncPingHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(
            Pair(AnAsyncPingHandler::class.java, pingHandler),
            Pair(AnotherAsyncPingHandler::class.java, anotherPingHandler)
        )
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.publishNotificationAsync(ExtendedPing())

        assertTrue {
            asyncCountDownLatch.count == 0L
        }
    }

    @Test
    fun inherited_notification_handler_should_be_called() = runBlocking {
        class PingForInherited : Notification

        abstract class NotificationHandlerBase<TNotification : Notification> : AsyncNotificationHandler<TNotification>

        class InheritedNotificationHandler : NotificationHandlerBase<PingForInherited>() {
            override suspend fun handle(notification: PingForInherited) {
                asyncCountDownLatch.countDown()
            }
        }

        val nHandler = InheritedNotificationHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(InheritedNotificationHandler::class.java, nHandler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.publishNotificationAsync(PingForInherited())

        assertEquals(0, asyncCountDownLatch.count)
    }

    @Nested
    inner class ParamaterizedTests {
        inner class ParameterizedNotification<T>(val param: T) : Notification

        inner class ParameterizedAsyncNotificationHandler<A> : AsyncNotificationHandler<ParameterizedNotification<A>> {
            override suspend fun handle(notification: ParameterizedNotification<A>) {
                asyncCountDownLatch.countDown()
            }
        }

        inner class ParameterizedNotificationHandler<A> : NotificationHandler<ParameterizedNotification<A>> {
            override fun handle(notification: ParameterizedNotification<A>) {
                countDownLatch.countDown()
            }
        }

        @Test
        fun `async notification should be fired`() = runBlocking {
            // given
            val handler = ParameterizedAsyncNotificationHandler<ParameterizedNotification<String>>()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(Pair(ParameterizedAsyncNotificationHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            bus.publishNotificationAsync(ParameterizedNotification("MyParam"))

            // then
            assertTrue {
                asyncCountDownLatch.count == 0L
            }
        }

        @Test
        fun `notification should be fired`() = runBlocking {
            // given
            val handler = ParameterizedNotificationHandler<ParameterizedNotification<String>>()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(Pair(ParameterizedNotificationHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            bus.publishNotification(ParameterizedNotification("MyParam"))

            // then
            assertTrue {
                countDownLatch.count == 0L
            }
        }

        @Test
        fun inherited_notification_handler_should_be_called() = runBlocking {
            var invocationCount = 0
            var parameter = ""

            class ParameterizedNotification<T>(val param: T) : Notification

            abstract class NotificationHandlerBase<TNotification : Notification> : AsyncNotificationHandler<TNotification>

            class ParameterizedAsyncNotificationHandler<A> : NotificationHandlerBase<ParameterizedNotification<A>>() {
                override suspend fun handle(notification: ParameterizedNotification<A>) {
                    parameter = notification.param.toString()
                    invocationCount++
                }
            }

            val nHandler = ParameterizedAsyncNotificationHandler<ParameterizedNotification<String>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedAsyncNotificationHandler::class.java, nHandler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()
            bus.publishNotificationAsync(ParameterizedNotification("invoked"))

            assertEquals(1, invocationCount)
            assertEquals("invoked", parameter)
        }
    }
}
