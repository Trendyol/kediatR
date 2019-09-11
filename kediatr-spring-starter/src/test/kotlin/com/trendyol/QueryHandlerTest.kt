package com.trendyol

import com.trendyol.kediatr.*
import com.trendyol.kediatr.spring.KediatrConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [KediatrConfiguration::class, TestQueryHandler::class, AsyncTestQueryHandler::class])
class QueryHandlerTest {

    @Autowired
    lateinit var commandBus: CommandBus

    @Test
    fun `queryHandler should retrieve result`() {
        val result = commandBus.executeQuery(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `async queryHandler should retrieve result`() = runBlocking {
        val result = commandBus.executeQueryAsync(TestQuery(1)).await()

        assertTrue {
            result == "hello 1"
        }
    }
}

class TestQuery(val id: Int) : Query<String>

class TestQueryHandler(): QueryHandler<String, TestQuery> {
    override fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}

class AsyncTestQueryHandler(): AsyncQueryHandler<String, TestQuery> {
    override suspend fun handleAsync(query: TestQuery): String {
        return "hello " + query.id
    }
}