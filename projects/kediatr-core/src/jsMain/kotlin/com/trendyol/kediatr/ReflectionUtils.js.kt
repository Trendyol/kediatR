package com.trendyol.kediatr

import kotlin.reflect.KClass

public actual object ReflectionUtils {
    actual fun isAbstract(kClass: KClass<*>): Boolean {
        // JS reflection is limited, we assume classes passed here are not abstract
        return false
    }

    actual fun <T : Any> isAssignableFrom(superType: KClass<T>, subType: KClass<*>): Boolean {
        // Check if subType is the same as superType
        if (superType == subType) return true
        
        // For JS platforms, we have limited reflection capabilities
        // This is a simplified implementation
        return false
    }

    actual fun getSuperclass(kClass: KClass<*>): KClass<*>? {
        // Kotlin/JS has limited reflection support
        return null
    }

    actual fun getHandlerParameterType(handlerClass: KClass<*>, handlerInterface: KClass<*>): KClass<*>? {
        // Kotlin/JS doesn't support full generic type reflection
        // This functionality requires manual registration
        return null
    }
}

