package com.trendyol.kediatr

interface DependencyProvider {
    fun <T> getTypeFor(clazz: Class<T>): T

    fun <T>getSubTypesOf(clazz: Class<T>): Collection<Class<T>>
}