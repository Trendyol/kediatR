package com.trendyol

import com.trendyol.kediatr.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QueryHandlerTest {

    @Test
    fun `queryHandler should retrieve result`() {
        val handler = TestQueryHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(TestQueryHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
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
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val result = bus.executeQueryAsync(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun `should throw exception if given async query has not been registered before`() {
        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManualDependencyProvider(handlers)
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
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            bus.executeQuery(NonExistQuery())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistQuery")
    }

    @Nested
    inner class ParamaterizedTests {
        inner class ParameterizedQuery<TParam, TResponse>(val param: TParam) : Query<TResponse>

        inner class ParameterizedAsyncQueryHandler<TParam> : AsyncQueryHandler<ParameterizedQuery<TParam, String>, String> {
            override suspend fun handleAsync(query: ParameterizedQuery<TParam, String>): String {
                return query.param.toString()
            }
        }

        inner class ParameterizedQueryHandler<TParam> : QueryHandler<ParameterizedQuery<TParam, String>, String> {
            override fun handle(query: ParameterizedQuery<TParam, String>): String {
                return query.param.toString()
            }
        }

        @Test
        fun `async query should be fired and return result`() = runBlocking {
            // given
            val handler = ParameterizedAsyncQueryHandler<ParameterizedQuery<Long, String>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedAsyncQueryHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            val result = bus.executeQueryAsync(ParameterizedQuery<Long, String>(61L))

            // then
            assertEquals(result, "61")
        }

        @Test
        fun `query should be fired and return result`() {
            // given
            val handler = ParameterizedQueryHandler<ParameterizedQuery<Long, String>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedQueryHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            val result = bus.executeQuery(ParameterizedQuery<Long, String>(61L))

            // then
            assertEquals(result, "61")
        }
    }
}

private class NonExistQuery : Query<String>

private class TestQuery(val id: Int) : Query<String>

private class TestQueryHandler : QueryHandler<TestQuery, String> {
    override fun handle(query: TestQuery): String {
        return "hello " + query.id
    }
}

private class AsyncTestQueryHandler : AsyncQueryHandler<TestQuery, String> {
    override suspend fun handleAsync(query: TestQuery): String {
        return "hello " + query.id
    }
}
