package com.trendyol.kediatr

interface QueryHandler<R, Q : Query<R>> {
    fun handle(query: Q): R
}

