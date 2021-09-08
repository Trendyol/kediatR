package com.trendyol.kediatr.koin

import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.DependencyProvider
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.component.getScopeName
import org.koin.java.KoinJavaComponent.getKoin
import org.reflections.Reflections
import kotlin.reflect.KClass

@OptIn(KoinInternalApi::class)
class KediatrKoinProvider : DependencyProvider {
    private val koin = getKoin()
    private var reflections: Reflections

    init {
        val aPackage = koin.instanceRegistry.instances.entries.map { it.value.beanDefinition.definition.getScopeName().type.java.`package` }.first().name
        val mainPackageName = Package.getPackages().filter { aPackage.startsWith(it.name) }.map { it.name }
        reflections = Reflections(mainPackageName)
    }

    override fun <T> getSingleInstanceOf(clazz: Class<T>): T {
        return koin.get(clazz.kClass())
    }

    override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> {
        return reflections.getSubTypesOf(clazz).map { it as Class<T> }
    }

    private fun <T> Class<T>.kClass(): KClass<out Any> {
        return (this as Class<*>).kotlin
    }
}

class KediatrKoin {
    companion object {
        fun getCommandBus() = CommandBusBuilder(KediatrKoinProvider()).build()
    }
}