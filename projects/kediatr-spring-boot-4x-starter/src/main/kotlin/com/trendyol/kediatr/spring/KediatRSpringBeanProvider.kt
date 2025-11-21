@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.spring

import com.trendyol.kediatr.DependencyProvider
import org.springframework.context.ApplicationContext

class KediatRSpringBeanProvider(
  private val applicationContext: ApplicationContext
) : DependencyProvider {
  override fun <T> getSingleInstanceOf(clazz: Class<T>): T = applicationContext
    .getBeanNamesForType(clazz)
    .single()
    .let { applicationContext.getBean(it) as T }

  override fun <T> getSubTypesOf(
    clazz: Class<T>
  ): Collection<Class<T>> = applicationContext
    .getBeanNamesForType(clazz)
    .map { applicationContext.getType(it) as Class<T> }
}
