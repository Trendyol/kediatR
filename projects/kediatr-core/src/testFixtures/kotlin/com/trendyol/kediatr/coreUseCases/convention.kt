package com.trendyol.kediatr.coreUseCases

import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.MediatorBuilder

@Suppress("UNCHECKED_CAST")
abstract class MediatorTestConvention {
  protected fun newMediator(handlers: List<Any> = emptyList()): Mediator = createMediator(handlers)

  private fun createMediator(
    handlers: List<Any> = emptyList()
  ): Mediator {
    val provider = MappingDependencyProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>)
    return MediatorBuilder(provider).build()
  }
}
