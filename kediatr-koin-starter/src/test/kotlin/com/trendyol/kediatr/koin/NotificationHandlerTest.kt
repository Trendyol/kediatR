package com.trendyol.kediatr.koin

import com.trendyol.kediatr.*
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
                single { MyPipelineBehavior(get()) } bind PipelineBehavior::class
                single { MyAsyncPipelineBehavior(get()) } bind MyAsyncPipelineBehavior::class
                single { MyFirstNotificationHandler(get()) } bind NotificationHandler::class
                single { MyFirstAsyncNotificationHandler(get()) } bind AsyncNotificationHandler::class
                single { MySecondNotificationHandler(get()) } bind NotificationHandler::class
            },
        )
    }

    init {
        notificationTestCounter = 0
        asyncNotificationTestCounter = 0
    }

    private val commandBus by inject<CommandBus>()

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

class MyFirstNotificationHandler(
    private val commandBus: CommandBus
) : NotificationHandler<MyNotification> {
    override fun handle(notification: MyNotification) {
        notificationTestCounter++
    }
}

class MySecondNotificationHandler(
    private val commandBus: CommandBus
) : NotificationHandler<MyNotification> {
    override fun handle(notification: MyNotification) {
        notificationTestCounter++
    }
}

class MyFirstAsyncNotificationHandler(
    private val commandBus: CommandBus
) : AsyncNotificationHandler<MyNotification> {
    override suspend fun handle(notification: MyNotification) {
        delay(500)
        asyncNotificationTestCounter++
    }
}


