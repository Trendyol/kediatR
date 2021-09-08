package com.trendyol.kediatr.common

import com.trendyol.kediatr.AsyncPipelineBehavior
import com.trendyol.kediatr.DependencyProvider
import com.trendyol.kediatr.PipelineBehavior

/**
 * PipelineProvider creates a pipeline behavior with enabled spring injection.
 *
 * @param <H> type of pipeline behavior
</H> */
internal class PipelineProvider<H : PipelineBehavior>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}

/**
 * AsyncPipelineProvider creates a async pipeline behavior with enabled spring injection.
 *
 * @param <H> type of pipeline behavior
</H> */
internal class AsyncPipelineProvider<H : AsyncPipelineBehavior>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}