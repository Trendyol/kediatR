package com.trendyol

import com.trendyol.kediatr.NotificationHandler
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.spring.KediatrConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

var notificationTestCounter = 0
var asyncNotificationTestCounter = 0

@SpringBootTest(
    classes = [
        KediatrConfiguration::class,
        MyFirstNotificationHandler::class
    ]
)
class NotificationHandlerTest {

    init {
        notificationTestCounter = 0
        asyncNotificationTestCounter = 0
    }

    @Autowired
    lateinit var commandBus: Mediator

    @Test
    fun `async notificationHandler should be fired`() = runBlocking {
        commandBus.publish(MyNotification())

        assertTrue {
            asyncNotificationTestCounter == 1
        }
    }
}

class MyNotification : Notification

class MyFirstNotificationHandler : NotificationHandler<MyNotification> {
    override suspend fun handle(notification: MyNotification) {
        delay(500)
        asyncNotificationTestCounter++
    }
}
