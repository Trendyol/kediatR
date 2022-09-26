package com.trendyol

import com.trendyol.kediatr.*
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

@ApplicationScoped
@Startup
class MyFirstNotificationHandler(
    private val commandBus: CommandBus,
) : NotificationHandler<MyNotification> {
    override fun handle(notification: MyNotification) {
        notificationTestCounter++
    }
}

@ApplicationScoped
@Startup
class MySecondNotificationHandler(
    private val commandBus: CommandBus,
) : NotificationHandler<MyNotification> {
    override fun handle(notification: MyNotification) {
        notificationTestCounter++
    }
}

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
