@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.spring

import com.trendyol.kediatr.DependencyProvider
import org.springframework.context.ApplicationContext

class KediatRSpringBeanProvider(
  private val applicationContext: ApplicationContext
) : DependencyProvider {
  override fun <T> getSingleInstanceOf(clazz: Class<T>): T {
    // Get bean by name first to ensure we get the Spring AOP proxy version
    // This is critical for suspend functions with @Transactional and other AOP annotations
    val beanNames = applicationContext.getBeanNamesForType(clazz)
    return if (beanNames.isNotEmpty()) {
      applicationContext.getBean(beanNames.first(), clazz)
    } else {
      applicationContext.getBean(clazz)
    }
  }

  override fun <T> getSubTypesOf(
    clazz: Class<T>
  ): Collection<Class<T>> = applicationContext
    .getBeanNamesForType(clazz)
    .map { applicationContext.getType(it) as Class<T> }
}
