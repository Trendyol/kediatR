package com.trendyol

import com.trendyol.kediatr.AsyncNotificationHandler
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.Notification
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
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
    lateinit var commandBus: CommandBus

    @Test
    fun `async notificationHandler should be fired`() = runBlocking {
        commandBus.publishNotificationAsync(MyNotification())

        assertTrue {
            asyncNotificationTestCounter == 1
        }
    }
}

class MyNotification : Notification

@ApplicationScoped
@Startup
class MyFirstAsyncNotificationHandler(
    private val commandBus: CommandBus,
) : AsyncNotificationHandler<MyNotification> {
    override suspend fun handle(notification: MyNotification) {
        delay(500)
        asyncNotificationTestCounter++
    }
}