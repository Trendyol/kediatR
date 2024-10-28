@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.quarkus

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.spi.*
import java.lang.reflect.*

@ApplicationScoped
class QuarkusTypeScanner(
  private val beanManager: BeanManager
) {
  fun <T> getSubTypesOf(
    clazz: Class<T>
  ): Collection<Class<T>> = beanManager.getBeans(Any::class.java)
    .asSequence()
    .filterNot(::quarkusThings)
    .flatMap { it.types }
    .mapNotNull { type -> mapRelevantOrNull(type, clazz) }
    .filter { it != clazz }
    .distinct()
    .toList()

  private fun quarkusThings(it: Bean<*>) = it.beanClass.packageName.contains("io.quarkus")

  private fun <T> mapRelevantOrNull(
    type: Type?,
    clazz: Class<T>
  ) = when (type) {
    is ParameterizedType -> getMatchingClass(type.rawType as Class<*>, clazz) ?: getMatchingGenericArgument(type, clazz)
    is Class<*> -> getMatchingClass(type, clazz)
    else -> null
  }

  private fun <T> getMatchingGenericArgument(
    type: ParameterizedType,
    clazz: Class<T>
  ) = type.actualTypeArguments.firstNotNullOfOrNull { arg -> (arg as? Class<*>)?.let { getMatchingClass(it, clazz) } }

  private fun <T> getMatchingClass(type: Class<*>, targetClass: Class<T>): Class<T>? = when {
    targetClass.isAssignableFrom(type) -> type as Class<T>
    else -> null
  }
}
