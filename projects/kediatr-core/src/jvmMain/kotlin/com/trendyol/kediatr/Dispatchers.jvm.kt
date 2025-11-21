package com.trendyol.kediatr

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val DefaultDispatcher: CoroutineDispatcher = Dispatchers.IO

