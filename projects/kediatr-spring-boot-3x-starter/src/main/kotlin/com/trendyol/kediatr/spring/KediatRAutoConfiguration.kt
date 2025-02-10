package com.trendyol.kediatr.spring

import com.trendyol.kediatr.*
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
open class KediatRAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  open fun kediatRSpringBeanProvider(
    applicationContext: ApplicationContext
  ): KediatRSpringBeanProvider = KediatRSpringBeanProvider(applicationContext)

  @Bean
  @ConditionalOnMissingBean
  open fun mediator(
    kediatRSpringBeanProvider: KediatRSpringBeanProvider
  ): Mediator = MediatorBuilder(kediatRSpringBeanProvider).build()
}
