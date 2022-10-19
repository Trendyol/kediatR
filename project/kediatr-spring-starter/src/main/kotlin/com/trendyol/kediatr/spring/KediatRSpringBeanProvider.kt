@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.spring

import com.trendyol.kediatr.DependencyProvider
import org.springframework.context.ApplicationContext

class KediatRSpringBeanProvider(
    private val applicationContext: ApplicationContext,
) : DependencyProvider {
    override fun <T> getSingleInstanceOf(clazz: Class<T>): T {
        return applicationContext.getBean(clazz)
    }

    override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> {
        return applicationContext.getBeanNamesForType(clazz)
            .map { applicationContext.getType(it) as Class<T> }
    }
}
