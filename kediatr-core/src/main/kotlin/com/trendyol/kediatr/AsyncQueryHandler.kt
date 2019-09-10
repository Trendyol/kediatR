package com.trendyol.kediatr

interface AsyncQueryHandler<R, Q : Query<R>> {
    suspend fun handleAsync(query: Q): R
}