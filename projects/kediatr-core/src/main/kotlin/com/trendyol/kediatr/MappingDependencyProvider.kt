@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

import java.lang.reflect.ParameterizedType

/**
 * Dependency provider that uses a map to resolve dependencies.
 * @param handlerMap A map that contains the handlers.
 * The key is the handler class and the value is the handler instance.
 * @see DependencyProvider
 */
class MappingDependencyProvider(
  private val handlerMap: HashMap<Class<*>, Any>
) : DependencyProvider {
  override fun <T> getSingleInstanceOf(clazz: Class<T>): T = handlerMap[clazz] as T

  override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> = handlerMap.keys
    .filter { isCompatibleType(it, clazz) }
    .map { it as Class<T> }

  private fun <T> isCompatibleType(
    handler: Class<*>,
    interfaceOrBaseClass: Class<T>
  ): Boolean = when {
    interfaceOrBaseClass.isAssignableFrom(handler) -> true
    handler.genericInterfaces
      .filterIsInstance<ParameterizedType>()
      .any { it.rawType == interfaceOrBaseClass } -> true

    else -> when (val superclass = handler.genericSuperclass) {
      is ParameterizedType -> {
        val inheritedHandler = superclass.rawType as Class<*>
        inheritedHandler.genericInterfaces
          .filterIsInstance<ParameterizedType>()
          .any { it.rawType == interfaceOrBaseClass }
      }

      is Class<*> -> interfaceOrBaseClass.isAssignableFrom(superclass)
      else -> false
    }
  }

  companion object {
    /**
     * Creates a mediator with the given handlers.
     * @param handlers The handlers to be used by the mediator. Can also be [PipelineBehavior] instances.
     * @return A mediator instance.
     */
    fun createMediator(handlers: List<Any> = emptyList()): Mediator {
      val provider = MappingDependencyProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>)
      val mediator = MediatorBuilder(provider).build()
      return mediator
    }
  }
}
