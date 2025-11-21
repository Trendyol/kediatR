package com.trendyol.kediatr

actual fun <K, V> createConcurrentMap(): MutableMap<K, V> {
    // JavaScript is single-threaded, so a regular mutableMapOf is sufficient
    return mutableMapOf()
}

