package com.trendyol.kediatr

import kotlin.reflect.KClass

public actual object ReflectionUtils {
    actual fun isAbstract(kClass: KClass<*>): Boolean {
        // Native reflection is limited, we assume classes passed here are not abstract
        // This is a simplification for native platforms
        return false
    }

    actual fun <T : Any> isAssignableFrom(superType: KClass<T>, subType: KClass<*>): Boolean {
        // Check if subType is the same as superType or inherits from it
        if (superType == subType) return true
        
        // For native platforms, we have limited reflection capabilities
        // This is a simplified implementation
        var current: KClass<*>? = subType
        while (current != null) {
            if (current == superType) return true
            // Native Kotlin doesn't have full reflection for superclass traversal
            // This is a limitation of Kotlin/Native
            current = null
        }
        return false
    }

    actual fun getSuperclass(kClass: KClass<*>): KClass<*>? {
        // Kotlin/Native has limited reflection support
        // We cannot reliably get superclass information
        return null
    }

    actual fun getHandlerParameterType(handlerClass: KClass<*>, handlerInterface: KClass<*>): KClass<*>? {
        // Kotlin/Native doesn't support full generic type reflection
        // This functionality requires manual registration or code generation
        // For now, return null to indicate this feature is not available on native platforms
        return null
    }
}

