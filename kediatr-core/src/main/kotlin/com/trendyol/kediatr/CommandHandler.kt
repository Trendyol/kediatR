package com.trendyol.kediatr

interface CommandHandler<TCommand : Command> {
    fun handle(command: TCommand)
}


