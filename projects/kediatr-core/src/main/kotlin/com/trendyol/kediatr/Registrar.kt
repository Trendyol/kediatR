package com.trendyol.kediatr

import java.lang.reflect.*

@Suppress("UNCHECKED_CAST")
abstract class Registrar {
  protected inline fun <reified THandler : Any, TParameter> registerFor(
    dependencyProvider: DependencyProvider,
    registrar: (key: Class<TParameter>, value: Class<THandler>) -> Unit
  ) = dependencyProvider.getSubTypesOf(THandler::class.java).forEach {
    registerFor<THandler, TParameter>(it) { key, value ->
      registrar(key as Class<TParameter>, value as Class<THandler>)
    }
  }

  protected inline fun <reified THandler : Any, TParameter> registerFor(
    handler: Class<*>,
    registrar: (key: Class<*>, value: Class<*>) -> Unit
  ) {
    val interfaceOrBaseClass = THandler::class.java
    if (!interfaceOrBaseClass.isAssignableFrom(handler)) return

    handler.genericInterfaces
      .filterIsInstance<ParameterizedType>()
      .map { extractParameter(it) }
      .forEach { registrar(it, handler) }

    when (handler.genericSuperclass) {
      is ParameterizedType -> {
        val inheritedHandler = (handler.genericSuperclass as ParameterizedType).rawType as Class<*>
        inheritedHandler.genericInterfaces
          .filterIsInstance<ParameterizedType>()
          .map { extractParameter(handler.genericSuperclass as ParameterizedType) }
          .forEach { registrar(it, handler) }
      }

      is Class<*> -> {
        val inheritedHandler = handler.genericSuperclass as Class<*>
        if (interfaceOrBaseClass.isAssignableFrom(inheritedHandler)) {
          inheritedHandler.genericInterfaces
            .filterIsInstance<ParameterizedType>()
            .map { extractParameter(it) }
            .forEach { registrar(it, handler) }
        }
      }
    }
  }

  protected inline fun <reified T> registerFor(
    dependencyProvider: DependencyProvider,
    registrar: (value: Class<T>) -> Unit
  ) = dependencyProvider.getSubTypesOf(T::class.java).forEach { handler ->
    registerFor<T>(handler) { value -> registrar(value as Class<T>) }
  }

  protected inline fun <reified T> registerFor(
    handler: Class<*>,
    registrar: (value: Class<*>) -> Unit
  ) {
    val interfaceOrBaseClass = T::class.java
    if (!interfaceOrBaseClass.isAssignableFrom(handler)) return
    registrar(handler)
  }

  protected fun extractParameter(genericInterface: ParameterizedType): Class<*> =
    when (val typeArgument = genericInterface.actualTypeArguments[0]) {
      is ParameterizedType -> typeArgument.rawType as Class<*>
      is TypeVariable<*> -> {
        val rawType = (genericInterface.rawType as Class<*>)
        when {
          rawType.genericInterfaces.any() -> extractParameter(rawType.genericInterfaces[0] as ParameterizedType)
          else -> rawType
        }
      }

      else -> typeArgument as Class<*>
    }
}
