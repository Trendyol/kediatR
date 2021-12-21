package com.trendyol

import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.DependencyProvider
import io.quarkus.runtime.Startup
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.spi.Bean
import javax.enterprise.inject.spi.BeanManager

class KediatrBeanProvider(
    private val beanManager: BeanManager
) : DependencyProvider {
    private val classHandlerMap: ConcurrentHashMap<Class<Any>, Any> = ConcurrentHashMap()

    private var initialBeans: Set<Bean<*>> = emptySet()

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
        if (initialBeans.isEmpty()) {
            initialBeans = beanManager.getBeans(Object::class.java)
        }

        val kediatrHandlerBeans: MutableList<Class<T>> = mutableListOf()
        initialBeans.forEach { bean ->
            val isKediatrBean = bean.types.any { type ->
                type.typeName.startsWith(clazz.name)
            }
            if (isKediatrBean) {
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
    fun commandBus(kediatrBeanProvider: KediatrBeanProvider): CommandBus {
        return CommandBusBuilder(kediatrBeanProvider).build()
    }
}