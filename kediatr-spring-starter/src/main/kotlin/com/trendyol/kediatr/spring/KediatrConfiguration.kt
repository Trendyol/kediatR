package com.trendyol.kediatr.spring

import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.CommandBusImpl
import com.trendyol.kediatr.common.RegistryImpl
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
    open fun commandBus(kediatrSpringBeanProvider: KediatrSpringBeanProvider): CommandBus {
        return CommandBusBuilder(kediatrSpringBeanProvider).build()
    }
}
