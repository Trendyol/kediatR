package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
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
    lateinit var mediator: Mediator

    @Test
    fun `should throw exception if given async query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                mediator.send(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals("handler could not be found for com.trendyol.kediatr.quarkus.NonExistQuery", exception.message)
    }
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

@ApplicationScoped
@Startup
class TestQueryHandler(
    private val mediator: Mediator,
) : QueryHandler<TestQuery, String> {
    override suspend fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}
