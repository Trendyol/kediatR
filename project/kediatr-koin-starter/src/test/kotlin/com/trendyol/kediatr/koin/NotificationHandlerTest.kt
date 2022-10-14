package com.trendyol.kediatr.koin

import com.trendyol.kediatr.AsyncNotificationHandler
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.Notification
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.assertTrue

private var notificationTestCounter = 0
private var asyncNotificationTestCounter = 0

class NotificationHandlerTest : KoinTest {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { MyAsyncPipelineBehavior(get()) } bind MyAsyncPipelineBehavior::class
                single { MyFirstAsyncNotificationHandler(get()) } bind AsyncNotificationHandler::class
            }
        )
    }

    init {
        notificationTestCounter = 0
        asyncNotificationTestCounter = 0
    }

    private val commandBus by inject<CommandBus>()

    @Test
    fun `async notificationHandler should be fired`() = runBlocking {
        commandBus.publishNotificationAsync(MyNotification())

        assertTrue {
            asyncNotificationTestCounter == 1
        }
    }
}

class MyNotification : Notification

class MyFirstAsyncNotificationHandler(
    private val commandBus: CommandBus,
) : AsyncNotificationHandler<MyNotification> {
    override suspend fun handle(notification: MyNotification) {
        delay(500)
        asyncNotificationTestCounter++
    }
}
