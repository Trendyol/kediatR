package com.trendyol

import com.trendyol.kediatr.AsyncQueryHandler
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Query
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QueryHandlerTest {

    @Test
    fun async_queryHandler_should_retrieve_result() = runBlocking {
        class TestQuery(val id: Int) : Query<String>

        class AsyncTestQueryHandler : AsyncQueryHandler<TestQuery, String> {
            override suspend fun handleAsync(query: TestQuery): String {
                return "hello " + query.id
            }
        }

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
    fun should_throw_exception_if_given_async_query_has_not_been_registered_before() {
        class NonExistQuery : Query<String>

        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                bus.executeQueryAsync(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals("handler could not be found for ${NonExistQuery::class.java.typeName}", exception.message)
    }

    @Nested
    inner class ParamaterizedTests {
        inner class ParameterizedQuery<TParam, TResponse>(val param: TParam) : Query<TResponse>

        inner class ParameterizedAsyncQueryHandler<TParam> : AsyncQueryHandler<ParameterizedQuery<TParam, String>, String> {
            override suspend fun handleAsync(query: ParameterizedQuery<TParam, String>): String {
                return query.param.toString()
            }
        }

        @Test
        fun async_query_should_be_fired_and_return_result() = runBlocking {
            // given
            val handler = ParameterizedAsyncQueryHandler<ParameterizedQuery<Long, String>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedAsyncQueryHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            val result = bus.executeQueryAsync(ParameterizedQuery<Long, String>(61L))

            // then
            assertEquals("61", result)
        }
    }
}
