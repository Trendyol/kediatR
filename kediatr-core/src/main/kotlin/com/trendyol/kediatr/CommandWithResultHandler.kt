package com.trendyol.kediatr

/**
 * Interface to be implemented for a  command with result handler
 *
 * @since 1.0.16
 * @param TCommand any [Command] subclass to handle
 * @see Command
 * @see AsyncCommandHandler
 */
interface CommandWithResultHandler<TCommand : CommandWithResult<TResult>, TResult> {
    /**
     * Handles a command
     *
     * @param command the command to handle
     */
    fun handle(command: TCommand): TResult
}