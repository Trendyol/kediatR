package com.trendyol.kediatr

import kotlin.reflect.KClass

public expect object ReflectionUtils {
    fun isAbstract(kClass: KClass<*>): Boolean
    fun <T : Any> isAssignableFrom(superType: KClass<T>, subType: KClass<*>): Boolean
    fun getSuperclass(kClass: KClass<*>): KClass<*>?

    /**
     * Finds the generic parameter type of the handler interface implemented by handlerClass.
     * E.g. if handlerClass implements RequestHandler<MyRequest, String>, and handlerInterface is RequestHandler,
     * this returns MyRequest.
     */
    fun getHandlerParameterType(handlerClass: KClass<*>, handlerInterface: KClass<*>): KClass<*>?
}
