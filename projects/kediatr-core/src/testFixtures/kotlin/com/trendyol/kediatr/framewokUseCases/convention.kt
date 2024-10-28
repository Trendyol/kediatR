package com.trendyol.kediatr.framewokUseCases

import com.trendyol.kediatr.Mediator

interface MediatorDIConvention {
  fun provideMediator(): Mediator

  val testMediator get() = provideMediator()
}
