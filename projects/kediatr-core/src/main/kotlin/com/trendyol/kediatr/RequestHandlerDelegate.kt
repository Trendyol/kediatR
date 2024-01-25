package com.trendyol.kediatr

/**
 * RequestHandlerDelegate<TRequest, TResponse> represents to Command, Query or Notification handler
 */
typealias RequestHandlerDelegate<TRequest, TResponse> = suspend (TRequest) -> TResponse
