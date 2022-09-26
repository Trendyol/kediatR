package com.trendyol

import com.trendyol.kediatr.*
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
        MyFirstNotificationHandler::class,
        MySecondNotificationHandler::class,
        MyFirstAsyncNotificationHandler::class
    ]
)
class NotificationHandlerTest {

    init {
        notificationTestCounter = 0
        asyncNotificationTestCounter = 0
    }

    @Autowired
    lateinit var commandBus: CommandBus

    @Test
    fun `notificationHandler should be fired`() {
        commandBus.publishNotification(MyNotification())
        assertTrue {
            notificationTestCounter == 2
        }
    }

    @Test
    fun `async notificationHandler should be fired`() = runBlocking {
        commandBus.publishNotificationAsync(MyNotification())

        assertTrue {
            asyncNotificationTestCounter == 1
        }
    }
}

class MyNotification : Notification

class MyFirstNotificationHandler : NotificationHandler<MyNotification> {
    override fun handle(notification: MyNotification) {
        notificationTestCounter++
    }
}

class MySecondNotificationHandler : NotificationHandler<MyNotification> {
    override fun handle(notification: MyNotification) {
        notificationTestCounter++
    }
}

class MyFirstAsyncNotificationHandler : AsyncNotificationHandler<MyNotification> {
    override suspend fun handle(notification: MyNotification) {
        delay(500)
        asyncNotificationTestCounter++
    }
}
