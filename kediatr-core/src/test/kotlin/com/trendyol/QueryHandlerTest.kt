package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class QueryHandlerTest {

    @Test
    fun `queryHandler should retrieve result`() {
        val bus: CommandBus = CommandBusBuilder(TestQuery::class.java).build()
        val result = bus.executeQuery(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `async queryHandler should retrieve result`() = runBlocking {
        val bus: CommandBus = CommandBusBuilder(TestQuery::class.java).build()
        val result = bus.executeQueryAsync(TestQuery(1)).await()

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