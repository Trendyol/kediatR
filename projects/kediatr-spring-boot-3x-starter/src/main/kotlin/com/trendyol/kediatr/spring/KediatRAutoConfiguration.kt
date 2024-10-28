package com.trendyol.kediatr.spring

import com.trendyol.kediatr.*
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
open class KediatRAutoConfiguration {
  @Bean
  open fun kediatRSpringBeanProvider(
    applicationContext: ApplicationContext
  ): KediatRSpringBeanProvider = KediatRSpringBeanProvider(applicationContext)

  @Bean
  open fun mediator(
    kediatRSpringBeanProvider: KediatRSpringBeanProvider
  ): Mediator = MediatorBuilder(kediatRSpringBeanProvider).build()
}
