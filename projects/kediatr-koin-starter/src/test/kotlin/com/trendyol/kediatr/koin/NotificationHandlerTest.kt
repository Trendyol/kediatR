package com.trendyol.kediatr.koin

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.NotificationHandler
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
    val koinTestExtension =
        KoinTestExtension.create {
            modules(
                module {
                    single { KediatRKoin.getMediator() }
                    single { ExceptionPipelineBehavior() } bind ExceptionPipelineBehavior::class
                    single { LoggingPipelineBehavior() } bind LoggingPipelineBehavior::class
                    single { MyFirstNotificationHandler(get()) } bind NotificationHandler::class
                }
            )
        }

    init {
        notificationTestCounter = 0
        asyncNotificationTestCounter = 0
    }

    private val mediator by inject<Mediator>()

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

class MyFirstNotificationHandler(
    private val mediator: Mediator
) : NotificationHandler<MyNotification> {
    override suspend fun handle(notification: MyNotification) {
        delay(500)
        asyncNotificationTestCounter++
    }
}
