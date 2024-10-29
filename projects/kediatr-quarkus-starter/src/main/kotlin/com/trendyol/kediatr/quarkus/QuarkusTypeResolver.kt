@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.quarkus

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.spi.*
import java.lang.reflect.*
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class QuarkusTypeResolver(
  private val beanManager: BeanManager
) {
  private val resolveCache = ConcurrentHashMap<Class<*>, Any>()
  private val typesCache = ConcurrentHashMap<Class<*>, Collection<Class<*>>>()
  private val beanTypes = beanManager.getBeans(Any::class.java)
    .asSequence()
    .filterNot(::quarkusPackage)
    .flatMap { it.types }

  fun <T> resolveOrThrow(clazz: Class<T>): T = resolveCache.computeIfAbsent(clazz) {
    val bean = beanManager.getBeans(clazz).singleOrNull() ?: error("No bean found for $clazz")
    val ctx = beanManager.createCreationalContext(bean)
    beanManager.getReference(bean, clazz, ctx)
  } as T

  fun <T> resolveTypesOrEmpty(clazz: Class<T>): Collection<Class<T>> = typesCache.computeIfAbsent(clazz) {
    beanTypes
      .mapNotNull { type -> mapRelevantOrNull(type, clazz) }
      .filter { it != clazz }
      .distinct()
      .toList()
  } as Collection<Class<T>>

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
  ): Class<T>? = type.actualTypeArguments
    .filterIsInstance<Class<*>>()
    .firstNotNullOfOrNull { getMatchingClass(it, clazz) }

  private fun <T> getMatchingClass(
    type: Class<*>,
    targetClass: Class<T>
  ): Class<T>? = when {
    targetClass.isAssignableFrom(type) -> type as Class<T>
    else -> null
  }

  private fun quarkusPackage(it: Bean<*>) = it.beanClass.packageName.contains("io.quarkus")
}
