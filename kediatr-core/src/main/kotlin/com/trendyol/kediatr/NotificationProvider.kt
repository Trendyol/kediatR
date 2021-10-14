package com.trendyol.kediatr.common

import com.trendyol.kediatr.AsyncNotificationHandler
import com.trendyol.kediatr.DependencyProvider
import com.trendyol.kediatr.NotificationHandler

internal class NotificationProvider<H : NotificationHandler<*>>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}

internal class AsyncNotificationProvider<H : AsyncNotificationHandler<*>>(
    private val dependencyProvider: DependencyProvider,
    private val type: Class<H>
) {

    fun get(): H {
        return dependencyProvider.getSingleInstanceOf(type)
    }
}