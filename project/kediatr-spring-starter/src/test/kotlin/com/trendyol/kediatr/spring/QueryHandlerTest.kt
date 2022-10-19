package com.trendyol.kediatr.spring

import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(classes = [KediatRConfiguration::class, TestQueryHandler::class])
class QueryHandlerTest {

    @Autowired
    lateinit var mediator: Mediator

    @Test
    fun `async queryHandler should retrieve result`() = runBlocking {
        val result = mediator.send(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `should throw exception if given async query does not have handler bean`() {
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                mediator.send(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals("handler could not be found for com.trendyol.kediatr.spring.NonExistQuery", exception.message)
    }
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

class TestQueryHandler : QueryHandler<TestQuery, String> {
    override suspend fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}
