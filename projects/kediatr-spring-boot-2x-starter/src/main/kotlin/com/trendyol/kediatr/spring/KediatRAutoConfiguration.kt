package com.trendyol.kediatr.spring

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.MediatorBuilder
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
open class KediatRAutoConfiguration {
    @Bean
    open fun kediatRSpringBeanProvider(applicationContext: ApplicationContext): KediatRSpringBeanProvider {
        return KediatRSpringBeanProvider(applicationContext)
    }

    @Bean
    open fun mediator(kediatRSpringBeanProvider: KediatRSpringBeanProvider): Mediator {
        return MediatorBuilder(kediatRSpringBeanProvider).build()
    }
}
