@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.spring

import com.trendyol.kediatr.DependencyProvider
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

class KediatRSpringBeanProvider(
  private val applicationContext: ApplicationContext
) : DependencyProvider {
  override fun <T : Any> getSingleInstanceOf(clazz: KClass<T>): T = applicationContext.getBean(clazz.java)

  override fun <T : Any> getSubTypesOf(
    clazz: KClass<T>
  ): Collection<KClass<T>> = applicationContext
    .getBeanNamesForType(clazz.java)
    .map { applicationContext.getType(it)!!.kotlin as KClass<T> }
}
