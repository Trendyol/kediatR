package com.trendyol.kediatr.koin

import com.trendyol.kediatr.AsyncQueryHandler
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Query
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

class QueryHandlerTest : KoinTest {
    private val commandBus by inject<CommandBus>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { MyAsyncPipelineBehavior(get()) } bind MyAsyncPipelineBehavior::class
                single { AsyncTestQueryHandler(get()) } bind AsyncQueryHandler::class
            }
        )
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
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

class AsyncTestQueryHandler(
    private val commandBus: CommandBus,
) : AsyncQueryHandler<TestQuery, String> {
    override suspend fun handleAsync(query: TestQuery): String {
        return "hello " + query.id
    }
}
