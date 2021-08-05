package com.kediatrkoinstarter

import com.trendyol.kediatr.DependencyProvider
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.reflect.KClass

class KediatrKoinProvider(
    private val koin: Koin
) : DependencyProvider {
    override fun <T> getTypeFor(clazz: Class<T>): T {
        return koin.get(kClass(clazz))
    }

    private fun <T> kClass(clazz: Class<T>): KClass<out Any> {
        return (clazz as Class<*>).kotlin
    }

    @OptIn(KoinInternalApi::class)
    override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> {
        return koin.scopeRegistry.rootScope.getAll<T>(kClass(clazz)).map { it!!::class.java as Class<T> }
    }
}
