package com.trendyol.kediatr

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private object ConcurrentMapStorage {
    val maps = mutableMapOf<Int, MutableMap<*, *>>()
    private var nextId = 0
    
    fun <K, V> createMap(): MutableMap<K, V> {
        @Suppress("UNCHECKED_CAST")
        return mutableMapOf<K, V>()
    }
}

actual fun <K, V> createConcurrentMap(): MutableMap<K, V> {
    return ConcurrentMapStorage.createMap()
}

