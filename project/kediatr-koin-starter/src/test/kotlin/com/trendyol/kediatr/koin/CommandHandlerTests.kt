package com.trendyol.kediatr.koin

import com.trendyol.kediatr.AsyncCommandHandler
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandBus
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
                single { KediatrKoin.getCommandBus() }
                single { MyAsyncPipelineBehavior(get()) } bind MyAsyncPipelineBehavior::class
                single { MyAsyncCommandHandler(get()) } bind AsyncCommandHandler::class
            }
        )
    }

    private val commandBus by inject<CommandBus>()

    init {
        springTestCounter = 0
        springAsyncTestCounter = 0
    }

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        commandBus.executeCommandAsync(MyCommand())

        assertTrue {
            springAsyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.executeCommandAsync(NonExistCommand())
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

class MyAsyncCommandHandler(
    val commandBus: CommandBus,
) : AsyncCommandHandler<MyCommand> {
    override suspend fun handleAsync(command: MyCommand) {
        delay(500)
        springAsyncTestCounter++
    }
}
