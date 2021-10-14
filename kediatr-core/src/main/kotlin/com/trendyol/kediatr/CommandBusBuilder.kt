package com.trendyol.kediatr

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

    fun build(registry: Registry = RegistryImpl(dependencyProvider)): CommandBus {
        return CommandBusImpl(registry, publishStrategy)
    }
}