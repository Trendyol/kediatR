package com.trendyol.kediatr

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

internal class MediatorBuilderTest {
    @ParameterizedTest
    @MethodSource("strategies")
    fun `When a publish strategy is defined it should be set`(expectedStrategy: PublishStrategy) {
        // Arrange/Act
        val builder =
            MediatorBuilder(ManualDependencyProvider(hashMapOf()))
                .withPublishStrategy(expectedStrategy)

        // Assert
        assertEquals(expectedStrategy, builder.defaultPublishStrategy)
    }

    companion object {
        @JvmStatic
        fun strategies() =
            listOf(
                Arguments.of(ContinueOnExceptionPublishStrategy()),
                Arguments.of(ParallelNoWaitPublishStrategy()),
                Arguments.of(ParallelWhenAllPublishStrategy()),
                Arguments.of(StopOnExceptionPublishStrategy())
            )
    }
}
