package com.trendyol.kediatr

interface PipelineBehavior {
    fun <TRequest>preProcess(request: TRequest)

    fun <TRequest>postProcess(request: TRequest)
}
