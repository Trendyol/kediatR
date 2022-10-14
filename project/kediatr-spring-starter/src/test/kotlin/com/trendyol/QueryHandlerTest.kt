package com.trendyol

import com.trendyol.kediatr.*
import com.trendyol.kediatr.spring.KediatrConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.*

@SpringBootTest(classes = [KediatrConfiguration::class, AsyncTestQueryHandler::class])
class QueryHandlerTest {

    @Autowired
    lateinit var commandBus: CommandBus

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
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistQuery")
    }
}

class NonExistQuery : Query<String>
class TestQuery(val id: Int) : Query<String>

class AsyncTestQueryHandler : AsyncQueryHandler<TestQuery, String> {
    override suspend fun handleAsync(query: TestQuery): String {
        return "hello " + query.id
    }
}
