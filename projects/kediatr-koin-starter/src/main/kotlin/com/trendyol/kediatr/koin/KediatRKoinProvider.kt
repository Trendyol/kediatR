@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.koin

import com.trendyol.kediatr.*
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.reflect.KClass

@OptIn(KoinInternalApi::class)
class KediatRKoinProvider : DependencyProvider {
  private val koin: Koin = getKoin()
  private val subTypes: List<KClass<*>>
    get() = koin
      .instanceRegistry.instances
      .map { it.value.beanDefinition }
      .fold(mutableListOf<KClass<*>>()) { acc, beanDefinition ->
        acc.also {
          it.add(beanDefinition.primaryType)
          it.addAll(beanDefinition.secondaryTypes)
        }
      }.distinct()

  override fun <T> getSingleInstanceOf(clazz: Class<T>): T = koin.get(clazz.kClass())

  override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> = subTypes
    .filter { clazz.isAssignableFrom(it.java) }
    .map { it.java as Class<T> }

  private fun <T> Class<T>.kClass(): KClass<out Any> = (this as Class<*>).kotlin
}

class KediatRKoin {
  companion object {
    fun getMediator() = Mediator.build(KediatRKoinProvider())
  }
}
