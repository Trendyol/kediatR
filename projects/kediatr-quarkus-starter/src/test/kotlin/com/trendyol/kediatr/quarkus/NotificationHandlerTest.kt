package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.NotificationHandler
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

private var notificationTestCounter = 0
private var asyncNotificationTestCounter = 0

@QuarkusTest
class NotificationHandlerTest {
    init {
        notificationTestCounter = 0
        asyncNotificationTestCounter = 0
    }

    @Inject
    lateinit var mediator: Mediator

    @Test
    fun `async notificationHandler should be fired`() =
        runBlocking {
            mediator.publish(MyNotification())

            assertTrue {
                asyncNotificationTestCounter == 1
            }
        }
}

class MyNotification : Notification

@ApplicationScoped
@Startup
class MyFirstNotificationHandler(
    private val mediator: Mediator
) : NotificationHandler<MyNotification> {
    override suspend fun handle(notification: MyNotification) {
        delay(500)
        asyncNotificationTestCounter++
    }
}
