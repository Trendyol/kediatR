package com.trendyol.kediatr.spring

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.MediatorBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KediatrConfiguration {
    @Bean
    open fun kediatrSpringBeanProvider(applicationContext: ApplicationContext): KediatrSpringBeanProvider {
        return KediatrSpringBeanProvider(applicationContext)
    }

    @Bean
    open fun commandBus(kediatrSpringBeanProvider: KediatrSpringBeanProvider): Mediator {
        return MediatorBuilder(kediatrSpringBeanProvider).build()
    }
}
