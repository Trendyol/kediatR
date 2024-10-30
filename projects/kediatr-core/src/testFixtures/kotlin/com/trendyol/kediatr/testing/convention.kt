package com.trendyol.kediatr.testing

import com.trendyol.kediatr.Mediator

abstract class MediatorTestConvention {
  abstract fun provideMediator(): Mediator

  val testMediator by lazy { provideMediator() }
}
