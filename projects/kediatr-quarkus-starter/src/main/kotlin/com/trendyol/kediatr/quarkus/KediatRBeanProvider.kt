@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.spi.BeanManager

class KediatRBeanProvider(
  private val beanManager: BeanManager,
  private val quarkusTypeScanner: QuarkusTypeScanner
) : DependencyProvider {
  override fun <T> getSingleInstanceOf(clazz: Class<T>): T {
    val beans = beanManager.getBeans(clazz)
    val bean = beans.firstOrNull() ?: error("No bean found for class $clazz")
    val ctx = beanManager.createCreationalContext(bean)
    return beanManager.getReference(bean, clazz, ctx) as T
  }

  override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> = quarkusTypeScanner.getSubTypesOf(clazz)
}

@ApplicationScoped
class QuarkusMediatorBuilder {
  @ApplicationScoped
  fun kediatRBeanProvider(
    beanManager: BeanManager,
    quarkusTypeScanner: QuarkusTypeScanner
  ): KediatRBeanProvider =
    KediatRBeanProvider(beanManager, quarkusTypeScanner)

  @ApplicationScoped
  @Startup
  fun mediator(kediatRBeanProvider: KediatRBeanProvider): Mediator = MediatorBuilder(kediatRBeanProvider).build()
}
