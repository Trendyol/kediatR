package com.trendyol.kediatr

class CommandBusBuilder(private val clazzOfAnyHandler: Class<*>) {


    fun build(): CommandBus {
        return CommandBusImpl(RegistryImpl(clazzOfAnyHandler))
    }
}