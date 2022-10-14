package com.trendyol.kediatr

/**
 * PipelineProvider creates a pipeline behavior with enabled spring injection.
 *
 * @param <H> type of pipeline behavior
</H> */
internal class PipelineProvider<H : PipelineBehavior>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>,
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}
