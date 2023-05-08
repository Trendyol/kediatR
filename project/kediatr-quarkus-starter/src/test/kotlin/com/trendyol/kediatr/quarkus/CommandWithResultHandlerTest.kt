package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.CommandWithResult
import com.trendyol.kediatr.CommandWithResultHandler
import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Mediator
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private var testCounter = 0
private var asyncTestCounter = 0

@QuarkusTest
class CommandWithResultHandlerTest {

    init {
        testCounter = 0
        asyncTestCounter = 0
    }

    @Inject
    lateinit var mediator: Mediator

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        mediator.send(MyCommandR())

        assertTrue {
            asyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                mediator.send(NonExistCommandR())
            }
        }

        assertNotNull(exception)
        assertEquals( "handler could not be found for com.trendyol.kediatr.quarkus.NonExistCommandR",exception.message)
    }
}

class Result

class NonExistCommandR : CommandWithResult<Result>
class MyCommandR : CommandWithResult<Result>

@ApplicationScoped
@Startup
class MyAsyncCommandRHandler(
    val mediator: Mediator,
) : CommandWithResultHandler<MyCommandR, Result> {
    override suspend fun handle(command: MyCommandR): Result {
        delay(500)
        asyncTestCounter++

        return Result()
    }
}
