package com.trendyol.kediatr.koin

import com.trendyol.kediatr.CommandWithResultHandler
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.CommandWithResult
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

private var testCounter = 0
private var asyncTestCounter = 0

class CommandWithResultHandlerTest : KoinTest {
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { MyPipelineBehavior(get()) } bind MyPipelineBehavior::class
                single { MyAsyncCommandRHandler(get()) } bind CommandWithResultHandler::class
            }
        )
    }

    init {
        testCounter = 0
        asyncTestCounter = 0
    }

    private val commandBus by inject<Mediator>()

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        commandBus.send(MyCommandR())

        assertTrue {
            asyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.send(NonExistCommandR())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.kediatr.koin.NonExistCommandR")
    }
}

class Result

class NonExistCommandR : CommandWithResult<Result>
class MyCommandR : CommandWithResult<Result>

class MyAsyncCommandRHandler(
    val commandBus: Mediator,
) : CommandWithResultHandler<MyCommandR, Result> {
    override suspend fun handle(command: MyCommandR): Result {
        delay(500)
        asyncTestCounter++

        return Result()
    }
}
