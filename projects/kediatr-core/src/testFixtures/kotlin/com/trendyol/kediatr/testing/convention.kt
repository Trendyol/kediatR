package com.trendyol.kediatr.testing

import com.trendyol.kediatr.*

@Suppress("UNCHECKED_CAST")
abstract class MediatorTestConvention {
  abstract fun provideMediator(): Mediator

  val testMediator by lazy { provideMediator() }

  protected fun createMediator(types: List<Any> = emptyList()): Mediator {
    val provider = MappingDependencyProvider(types.associateBy { it.javaClass } as HashMap<Class<*>, Any>)
    val mediator = MediatorBuilder(provider).build()
    return mediator
  }
}
