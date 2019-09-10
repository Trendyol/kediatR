package com.trendyol.kediatr.spring

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KediatrConfiguration {

    @Bean
    open fun springBeanRegistry(applicationContext: ApplicationContext): SpringBeanRegistry {
        return SpringBeanRegistry(applicationContext)
    }

    @Bean
    open fun springCommandBus(springBeanRegistry: SpringBeanRegistry): SpringCommandBus {
        return SpringCommandBus(springBeanRegistry)
    }
}