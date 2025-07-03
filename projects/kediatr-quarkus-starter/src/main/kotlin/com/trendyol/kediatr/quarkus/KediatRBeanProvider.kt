package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped

class KediatRBeanProvider(
  private val resolver: QuarkusTypeResolver
) : DependencyProvider {
  override fun <T> getSingleInstanceOf(clazz: Class<T>): T = resolver.resolveOrThrow(clazz)

  override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> = resolver.resolveTypesOrEmpty(clazz)
}

@ApplicationScoped
class QuarkusMediatorBuilder {
  @ApplicationScoped
  fun kediatRBeanProvider(resolver: QuarkusTypeResolver): KediatRBeanProvider = KediatRBeanProvider(resolver)

  @ApplicationScoped
  @Startup
  fun mediator(kediatRBeanProvider: KediatRBeanProvider): Mediator = Mediator.build(kediatRBeanProvider)
}
