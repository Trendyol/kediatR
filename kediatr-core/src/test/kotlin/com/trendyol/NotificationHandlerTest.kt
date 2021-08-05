package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertTrue

private val asyncCountDownLatch = CountDownLatch(2)
private val countDownLatch = CountDownLatch(2)

open class Ping : Notification
class ExtendedPing : Ping()

class AnAsyncPingHandler : AsyncNotificationHandler<ExtendedPing> {
    override suspend fun handle(notification: ExtendedPing) {
        asyncCountDownLatch.countDown()
    }
}

class AnotherAsyncPingHandler : AsyncNotificationHandler<Ping> {
    override suspend fun handle(notification: Ping) {
        asyncCountDownLatch.countDown()
    }
}

class PingHandler : NotificationHandler<Ping> {
    override fun handle(notification: Ping) {
        countDownLatch.countDown()
    }
}

class NotificationHandlerTest {

    @Test
    fun `notification handler should be called`() {
        val pingHandler = PingHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(PingHandler::class.java, pingHandler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.publishNotification(Ping())

        assertTrue {
            countDownLatch.count == 1L
        }
    }


    @Test
    fun `async notification handler should be called`() = runBlocking {
        val pingHandler = AnAsyncPingHandler()
        val anotherPingHandler = AnotherAsyncPingHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AnAsyncPingHandler::class.java, pingHandler), Pair(AnotherAsyncPingHandler::class.java, anotherPingHandler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.publishNotificationAsync(ExtendedPing())

        assertTrue {
            asyncCountDownLatch.count == 0L
        }
    }
}
