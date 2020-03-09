package com.trendyol

import com.trendyol.kediatr.AsyncNotificationHandler
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.Notification
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertTrue

private val countDownLatch = CountDownLatch(2)

open class Ping : Notification
class ExtendedPing : Ping()

class AnAsyncPingHandler : AsyncNotificationHandler<ExtendedPing> {
    override suspend fun handle(notification: ExtendedPing) {
        countDownLatch.countDown()
    }
}

class AnotherAsyncPingHandler : AsyncNotificationHandler<Ping> {
    override suspend fun handle(notification: Ping) {
        countDownLatch.countDown()
    }
}

class NotificationHandlerTest {

    @Test
    fun `async notification handler should be called`() = runBlocking {
        val bus: CommandBus = CommandBusBuilder(Ping::class.java).build()
        bus.publishNotificationAsync(ExtendedPing())

        assertTrue {
            countDownLatch.count == 0L
        }
    }
}