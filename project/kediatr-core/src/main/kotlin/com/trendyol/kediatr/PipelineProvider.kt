package com.trendyol.kediatr

/**
 * AsyncPipelineProvider creates a async pipeline behavior with enabled spring injection.
 *
 * @param <H> type of pipeline behavior
</H> */
internal class AsyncPipelineProvider<H : AsyncPipelineBehavior>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>,
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}
