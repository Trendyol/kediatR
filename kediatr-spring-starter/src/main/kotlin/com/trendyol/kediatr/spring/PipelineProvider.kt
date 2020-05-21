package com.trendyol.kediatr.spring

import com.trendyol.kediatr.AsyncPipelineBehavior
import com.trendyol.kediatr.PipelineBehavior
import org.springframework.context.ApplicationContext

/**
 * PipelineProvider creates a pipeline behavior with enabled spring injection.
 *
 * @param <H> type of pipeline behavior
</H> */
internal class PipelineProvider<H : PipelineBehavior>(private val applicationContext: ApplicationContext, private val type: Class<H>) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}

/**
 * AsyncPipelineProvider creates a async pipeline behavior with enabled spring injection.
 *
 * @param <H> type of pipeline behavior
</H> */
internal class AsyncPipelineProvider<H : AsyncPipelineBehavior>(private val applicationContext: ApplicationContext, private val type: Class<H>) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}