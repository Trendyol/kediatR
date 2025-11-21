package com.trendyol.kediatr

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Platform-specific default dispatcher for I/O operations
 */
expect val DefaultDispatcher: CoroutineDispatcher

