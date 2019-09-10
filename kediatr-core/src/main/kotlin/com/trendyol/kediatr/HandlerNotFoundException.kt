package com.trendyol.kediatr

class HandlerNotFoundException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
}