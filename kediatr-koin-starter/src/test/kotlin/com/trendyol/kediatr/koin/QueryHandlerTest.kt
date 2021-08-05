package com.trendyol.kediatr.koin

import com.trendyol.kediatr.*
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

class QueryHandlerTest: KoinTest {
    private val commandBus by inject<CommandBus>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { MyPipelineBehavior(get()) } bind PipelineBehavior::class
                single { MyAsyncPipelineBehavior(get()) } bind MyAsyncPipelineBehavior::class
                single { TestQueryHandler(get()) } bind QueryHandler::class
                single { AsyncTestQueryHandler(get()) } bind AsyncQueryHandler::class
            },
        )
    }

    @Test
    fun `queryHandler should retrieve result`() {
        val result = commandBus.executeQuery(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `async queryHandler should retrieve result`() = runBlocking {
        val result = commandBus.executeQueryAsync(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }


    @Test
    fun `should throw exception if given async query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.executeQueryAsync(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.kediatr.koin.NonExistQuery")
    }

    @Test
    fun `should throw exception if given query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            commandBus.executeQuery(NonExistQuery())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.kediatr.koin.NonExistQuery")
    }
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

class TestQueryHandler(
    private val commandBus: CommandBus
) : QueryHandler<TestQuery, String> {
    override fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}

class AsyncTestQueryHandler(
    private val commandBus: CommandBus
) : AsyncQueryHandler<TestQuery, String> {
    override suspend fun handleAsync(query: TestQuery): String {
        return "hello " + query.id
    }
}