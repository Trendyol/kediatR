@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.koin

import com.trendyol.kediatr.*
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools
import kotlin.reflect.KClass

@OptIn(KoinInternalApi::class)
class KediatRKoinProvider : DependencyProvider {
  private val koin: Koin = KoinPlatformTools.defaultContext().get()
  private val subTypes: List<KClass<*>>
    get() {
      val instances = koin.instanceRegistry.instances
      if (instances.isEmpty()) {
          error("Koin instances are empty! Koin: $koin. Make sure beans are loaded.")
      }
      return instances
        .map { it.value.beanDefinition }
        .fold(mutableListOf<KClass<*>>()) { acc, beanDefinition ->
          acc.also {
            it.add(beanDefinition.primaryType)
            it.addAll(beanDefinition.secondaryTypes)
          }
        }.distinct()
    }

  override fun <T : Any> getSingleInstanceOf(clazz: KClass<T>): T = koin.get(clazz)

  override fun <T : Any> getSubTypesOf(clazz: KClass<T>): Collection<KClass<T>> {
    val allTypes = subTypes
    return allTypes
      .filter { ReflectionUtils.isAssignableFrom(clazz, it) }
      .map { it as KClass<T> }
  }
}

class KediatRKoin {
  companion object {
    fun getMediator() = Mediator.build(KediatRKoinProvider())
  }
}
