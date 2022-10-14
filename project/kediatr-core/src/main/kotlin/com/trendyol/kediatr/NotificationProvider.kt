package com.trendyol.kediatr

internal class NotificationProvider<H : NotificationHandler<*>>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>,
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}
