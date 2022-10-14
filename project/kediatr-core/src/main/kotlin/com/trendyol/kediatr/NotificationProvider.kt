package com.trendyol.kediatr

internal class AsyncNotificationProvider<H : AsyncNotificationHandler<*>>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>,
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}
