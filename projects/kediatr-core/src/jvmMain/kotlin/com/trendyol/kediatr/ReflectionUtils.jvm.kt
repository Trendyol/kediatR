package com.trendyol.kediatr

import kotlin.reflect.KClass
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable

public actual object ReflectionUtils {
    actual fun isAbstract(kClass: KClass<*>): Boolean {
        return Modifier.isAbstract(kClass.java.modifiers)
    }

    actual fun <T : Any> isAssignableFrom(superType: KClass<T>, subType: KClass<*>): Boolean {
        return superType.java.isAssignableFrom(subType.java)
    }

    actual fun getSuperclass(kClass: KClass<*>): KClass<*>? {
        return kClass.java.superclass?.kotlin
    }

    actual fun getHandlerParameterType(handlerClass: KClass<*>, handlerInterface: KClass<*>): KClass<*>? {
        val handlerJava = handlerClass.java
        val interfaceJava = handlerInterface.java
        
        if (!interfaceJava.isAssignableFrom(handlerJava)) return null
        
        return findParameterType(handlerJava, interfaceJava)?.kotlin
    }

    private fun findParameterType(handler: Class<*>, targetInterface: Class<*>): Class<*>? {
        // Check directly implemented interfaces
        handler.genericInterfaces
            .filterIsInstance<ParameterizedType>()
            .filter { 
                 val raw = it.rawType as Class<*>
                 targetInterface.isAssignableFrom(raw) 
            }
            .forEach { 
                return extractParameterFromRegistrarLogic(it)
            }

        // Check superclass
        val genericSuperclass = handler.genericSuperclass
        if (genericSuperclass is ParameterizedType) {
             val inheritedHandler = genericSuperclass.rawType as Class<*>
             if (targetInterface.isAssignableFrom(inheritedHandler)) {
                 return extractParameterFromRegistrarLogic(genericSuperclass)
             }
        } else if (genericSuperclass is Class<*>) {
             if (targetInterface.isAssignableFrom(genericSuperclass)) {
                  return findParameterType(genericSuperclass, targetInterface)
             }
        }
        
        return null
    }

    private fun extractParameterFromRegistrarLogic(genericInterface: ParameterizedType): Class<*> {
        return when (val typeArgument = genericInterface.actualTypeArguments[0]) {
          is ParameterizedType -> {
            typeArgument.rawType as Class<*>
          }
    
          is TypeVariable<*> -> {
            val rawType = (genericInterface.rawType as Class<*>)
            if (rawType.genericInterfaces.isNotEmpty()) {
                 extractParameterFromRegistrarLogic(rawType.genericInterfaces[0] as ParameterizedType)
            } else {
                 rawType
            }
          }
    
          else -> {
            typeArgument as Class<*>
          }
        }
    }
}
