package com.trendyol.kediatr.quarkus

import com.trendyol.kediatr.*
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import kotlin.reflect.KClass

class KediatRBeanProvider(
  private val resolver: QuarkusTypeResolver
) : DependencyProvider {
  override fun <T : Any> getSingleInstanceOf(clazz: KClass<T>): T = resolver.resolveOrThrow(clazz.java)

  override fun <T : Any> getSubTypesOf(clazz: KClass<T>): Collection<KClass<T>> = resolver
    .resolveTypesOrEmpty(clazz.java)
    .map { it.kotlin as KClass<T> }
}

@ApplicationScoped
class QuarkusMediatorBuilder {
  @ApplicationScoped
  fun kediatRBeanProvider(resolver: QuarkusTypeResolver): KediatRBeanProvider = KediatRBeanProvider(resolver)

  @ApplicationScoped
  @Startup
  fun mediator(kediatRBeanProvider: KediatRBeanProvider): Mediator = Mediator.build(kediatRBeanProvider)
}
