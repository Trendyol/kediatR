package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking command with result handler
 *
 * @since 1.0.16
 * @param TCommand any [Command] subclass to handle
 * @see Command
 * @see CommandHandler
 */
interface AsyncCommandWithResultHandler<TCommand : CommandWithResult<TResult>, TResult> {
    /**
     * Handles a command
     *
     * @param command the command to handle
     */
    suspend fun handleAsync(command: TCommand) : TResult
}