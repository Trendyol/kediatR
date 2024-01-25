@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

import java.lang.reflect.ParameterizedType

class ManualDependencyProvider(
    private val handlerMap: HashMap<Class<*>, Any>
) : DependencyProvider {
    override fun <T> getSingleInstanceOf(clazz: Class<T>): T {
        return handlerMap[clazz] as T
    }

    override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> {
        return handlerMap
            .filter { filterInternal(it.key, clazz) }
            .map {
                it.key as Class<T>
            }
    }

    private fun <THandler> filterInternal(
        handler: Class<*>,
        interfaceOrBaseClass: Class<THandler>
    ): Boolean {
        if (interfaceOrBaseClass.isAssignableFrom(handler)) return true

        if (handler.genericInterfaces
                .filterIsInstance<ParameterizedType>()
                .any { it.rawType == interfaceOrBaseClass }
        ) {
            return true
        }

        return when (handler.genericSuperclass) {
            is ParameterizedType -> {
                val inheritedHandler = (handler.genericSuperclass as ParameterizedType).rawType as Class<*>
                inheritedHandler.genericInterfaces
                    .filterIsInstance<ParameterizedType>()
                    .any { it.rawType == interfaceOrBaseClass }
            }

            is Class<*> -> {
                val inheritedHandler = (handler.genericSuperclass as Class<*>)
                interfaceOrBaseClass.isAssignableFrom(inheritedHandler)
            }

            else -> false
        }
    }
}
