package com.trendyol.kediatr
import java.util.concurrent.ConcurrentHashMap

actual fun <K, V> createConcurrentMap(): MutableMap<K, V> = ConcurrentHashMap()
