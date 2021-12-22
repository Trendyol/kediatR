package com.trendyol

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@QuarkusTest
class QueryHandlerTest {
    @Inject
    lateinit var commandBus: CommandBus

    @Test
    fun `queryHandler should retrieve result`() {
        val result = commandBus.executeQuery(TestQuery(1))

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
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistQuery")
    }

    @Test
    fun `should throw exception if given query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            commandBus.executeQuery(NonExistQuery())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistQuery")
    }
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

@ApplicationScoped
@Startup
class TestQueryHandler(
    private val commandBus: CommandBus
) : QueryHandler<TestQuery, String> {
    override fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}

@ApplicationScoped
@Startup
class AsyncTestQueryHandler(
    private val commandBus: CommandBus
) : AsyncQueryHandler<TestQuery, String> {
    override suspend fun handleAsync(query: TestQuery): String {
        return "hello " + query.id
    }
}