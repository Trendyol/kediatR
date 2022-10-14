@file:Suppress("UNCHECKED_CAST")

package com.trendyol

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.MediatorBuilder
import com.trendyol.kediatr.DependencyProvider
import io.quarkus.runtime.Startup
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.spi.Bean
import javax.enterprise.inject.spi.BeanManager

class KediatrBeanProvider(
    private val beanManager: BeanManager,
) : DependencyProvider {

    private val KEDIATR_PACKAGE_PREFIX = "com.trendyol.kediatr"
    private val classHandlerMap: ConcurrentHashMap<Class<Any>, Any> = ConcurrentHashMap()
    private var initialAllKediatrBeans: List<Bean<*>> = emptyList()

    override fun <T> getSingleInstanceOf(clazz: Class<T>): T {
        val instance = classHandlerMap.getOrPut(clazz as Class<Any>) {
            val beans = beanManager.getBeans(clazz)
            val bean = beanManager.resolve(beans)
            val context = beanManager.createCreationalContext(bean)
            beanManager.getReference(bean, bean.beanClass, context) as Any
        }
        return instance as T
    }

    override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> {
        if (initialAllKediatrBeans.isEmpty()) {
            initialAllKediatrBeans = beanManager.getBeans(Object::class.java).filter { bean ->
                bean.types.any { type ->
                    type.typeName.startsWith(KEDIATR_PACKAGE_PREFIX)
                }
            }
        }

        val kediatrHandlerBeans: MutableList<Class<T>> = mutableListOf()
        initialAllKediatrBeans.forEach { bean ->
            val isKediatrHandlerBean = bean.types.any { type ->
                type.typeName.startsWith(clazz.name)
            }
            if (isKediatrHandlerBean) {
                val kediatrHandler =
                    bean.types.first { !it.typeName.startsWith(clazz.name) && !it.typeName.equals(Object::class.java.name) }
                kediatrHandlerBeans.add(kediatrHandler as Class<T>)
            }
        }

        return kediatrHandlerBeans
    }
}

@ApplicationScoped
class QuarkusCommandBusBuilder {
    @ApplicationScoped
    fun kediatrBeanProvider(beanManager: BeanManager): KediatrBeanProvider {
        return KediatrBeanProvider(beanManager)
    }

    @ApplicationScoped
    @Startup
    fun commandBus(kediatrBeanProvider: KediatrBeanProvider): Mediator {
        return MediatorBuilder(kediatrBeanProvider).build()
    }
}
