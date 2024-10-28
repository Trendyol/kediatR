package com.trendyol.kediatr.coreUseCases

import com.trendyol.kediatr.HandlerNotFoundException
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

abstract class QueryHandlerUseCases : MediatorTestConvention() {
  @Test
  fun async_queryHandler_should_retrieve_result() = runTest {
    class TestQuery(val id: Int) : Query<String>

    class TestQueryHandler : QueryHandler<TestQuery, String> {
      override suspend fun handle(query: TestQuery): String {
        return "hello " + query.id
      }
    }

    val handler = TestQueryHandler()
    val bus: Mediator = newMediator(handlers = listOf(handler))
    val result = bus.send(TestQuery(1))

    result shouldBe "hello 1"
  }

  @Test
  fun should_throw_exception_if_given_async_query_has_not_been_registered_before() = runTest {
    class NonExistQuery : Query<String>

    val bus: Mediator = newMediator()
    val exception = shouldThrow<HandlerNotFoundException> {
      bus.send(NonExistQuery())
    }

    exception.message shouldBe "handler could not be found for ${NonExistQuery::class.java.typeName}"
  }

  inner class ParameterizedQuery<TParam, TResponse>(val param: TParam) : Query<TResponse>

  inner class ParameterizedQueryHandler<TParam> : QueryHandler<ParameterizedQuery<TParam, String>, String> {
    override suspend fun handle(query: ParameterizedQuery<TParam, String>): String {
      return query.param.toString()
    }
  }

  @Test
  fun async_query_should_be_fired_and_return_result() = runTest {
    // given
    val handler = ParameterizedQueryHandler<ParameterizedQuery<Long, String>>()
    val bus: Mediator = newMediator(handlers = listOf(handler))

    // when
    val result = bus.send(ParameterizedQuery<Long, String>(61L))

    // then
    result shouldBe "61"
  }
}
