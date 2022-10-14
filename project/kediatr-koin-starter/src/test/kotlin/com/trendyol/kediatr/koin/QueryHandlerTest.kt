package com.trendyol.kediatr.koin

import com.trendyol.kediatr.QueryHandler
import com.trendyol.kediatr.Mediator
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
    private val commandBus by inject<Mediator>()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { KediatrKoin.getCommandBus() }
                single { MyPipelineBehavior(get()) } bind MyPipelineBehavior::class
                single { TestQueryHandler(get()) } bind QueryHandler::class
            }
        )
    }

    @Test
    fun `should throw exception if given async query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.send(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.kediatr.koin.NonExistQuery")
    }
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

class TestQueryHandler(
    private val commandBus: Mediator,
) : QueryHandler<TestQuery, String> {
    override suspend fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}
