package com.trendyol

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import io.quarkus.test.junit.QuarkusTest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@QuarkusTest
class QueryHandlerTest {
    @Inject
    lateinit var commandBus: Mediator

    @Test
    fun `should throw exception if given async query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                commandBus.send(NonExistQuery())
            }
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
    private val commandBus: Mediator,
) : QueryHandler<TestQuery, String> {
    override suspend fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}
