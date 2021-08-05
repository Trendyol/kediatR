package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QueryHandlerTest {

    @Test
    fun `queryHandler should retrieve result`() {
        val handler = TestQueryHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(TestQueryHandler::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val result = bus.executeQuery(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `async queryHandler should retrieve result`() = runBlocking {
        val handler = AsyncTestQueryHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AsyncTestQueryHandler::class.java, handler))
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val result = bus.executeQueryAsync(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `should throw exception if given async query has not been registered before`() {
        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                bus.executeQueryAsync(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistQuery")
    }

    @Test
    fun `should throw exception if given query has not been registered before`() {
        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManuelDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            bus.executeQuery(NonExistQuery())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistQuery")
    }
}

class NonExistQuery : Query<String>

class TestQuery(val id: Int) : Query<String>

class TestQueryHandler : QueryHandler<TestQuery, String> {
    override fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}

class AsyncTestQueryHandler : AsyncQueryHandler<TestQuery, String> {
    override suspend fun handleAsync(query: TestQuery): String {
        return "hello " + query.id
    }
}
