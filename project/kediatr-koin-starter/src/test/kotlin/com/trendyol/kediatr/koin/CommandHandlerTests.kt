package com.trendyol.kediatr.koin

import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.HandlerNotFoundException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommandHandlerTests : KoinTest {
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatRKoin.getMediator() }
                single { ExceptionPipelineBehavior() } bind ExceptionPipelineBehavior::class
                single { LoggingPipelineBehavior() } bind LoggingPipelineBehavior::class
                single { MyCommandHandler(get()) } bind CommandHandler::class
            }
        )
    }

    private val mediator by inject<Mediator>()

    init {
        springTestCounter = 0
        springAsyncTestCounter = 0
    }

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        mediator.send(MyCommand())

        assertTrue {
            springAsyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                mediator.send(NonExistCommand())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.kediatr.koin.NonExistCommand")
    }
}

private var springTestCounter = 0
private var springAsyncTestCounter = 0

class NonExistCommand : Command
class MyCommand : Command

class MyCommandHandler(
    val mediator: Mediator,
) : CommandHandler<MyCommand> {
    override suspend fun handle(command: MyCommand) {
        delay(500)
        springAsyncTestCounter++
    }
}
