package com.trendyol.kediatr

import com.trendyol.kediatr.common.RegistryImpl

class CommandBusBuilder(
    private val dependencyProvider: DependencyProvider
) {
    private var publishStrategy: PublishStrategy = StopOnExceptionPublishStrategy()

    /**
     * Overrides default notification publishing strategy.
     * Default strategy is [StopOnExceptionPublishStrategy]
     *
     * @since 1.0.9
     * @see [PublishStrategy]
     * @see [ContinueOnExceptionPublishStrategy]
     * @see [StopOnExceptionPublishStrategy]
     * @see [ParallelNoWaitPublishStrategy]
     * @see [ParallelWhenAllPublishStrategy]
     */
    fun withPublishStrategy(publishStrategy: PublishStrategy): CommandBusBuilder {
        this.publishStrategy = publishStrategy
        return this
    }

    fun build(): CommandBus {
        return CommandBusImpl(RegistryImpl(dependencyProvider), publishStrategy)
    }
}