package com.trendyol.kediatr.coreUseCases

import com.trendyol.kediatr.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PublishStrategyTests {
  @Test
  fun `When a publish strategy is defined it should be set`() {
    listOf(
      ContinueOnExceptionPublishStrategy(),
      ParallelNoWaitPublishStrategy(),
      ParallelWhenAllPublishStrategy(),
      StopOnExceptionPublishStrategy()
    ).forEach {
      val builder = MediatorBuilder(MappingDependencyProvider(hashMapOf()))
        .withPublishStrategy(it)

      // Assert
      builder.defaultPublishStrategy shouldBe it
    }
  }
}
