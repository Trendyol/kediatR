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
        val bus: CommandBus = CommandBusBuilder(Ping::class.java).build()
        bus.publishNotification(Ping())

        assertTrue {
            countDownLatch.count == 1L
        }
    }


    @Test
    fun `async notification handler should be called`() = runBlocking {
        val bus: CommandBus = CommandBusBuilder(Ping::class.java).build()
        bus.publishNotificationAsync(ExtendedPing())

        assertTrue {
            asyncCountDownLatch.count == 0L
        }
    }
}