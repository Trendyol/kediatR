package com.trendyol.kediatr.spring

import com.trendyol.kediatr.AsyncNotificationHandler
import com.trendyol.kediatr.NotificationHandler
import org.springframework.context.ApplicationContext

internal class NotificationProvider<H : NotificationHandler<*>>(
    private val applicationContext: ApplicationContext,
    private val type: Class<H>
) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}

internal class AsyncNotificationProvider<H : AsyncNotificationHandler<*>>(
    private val applicationContext: ApplicationContext,
    private val type: Class<H>
) {

    fun get(): H {
        return applicationContext.getBean(type)
    }
}