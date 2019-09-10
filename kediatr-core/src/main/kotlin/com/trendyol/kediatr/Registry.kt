package com.trendyol.kediatr


interface Registry {

    fun <TCommand : Command> resolveCommandHandler(classOfCommand: Class<TCommand>): CommandHandler<TCommand>

    fun <TQuery : Query<TResult>, TResult> resolveQueryHandler(classOfQuery: Class<TQuery>): QueryHandler<TResult, TQuery>

    fun <TCommand : Command> resolveAsyncCommandHandler(classOfCommand: Class<TCommand>): AsyncCommandHandler<TCommand>

    fun <TQuery : Query<TResult>, TResult> resolveAsyncQueryHandler(classOfQuery: Class<TQuery>): AsyncQueryHandler<TResult, TQuery>
}