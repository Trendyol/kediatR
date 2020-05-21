package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking command handler
 *
 * @since 1.0.0
 * @param TCommand any [Command] subclass to handle
 * @see Command
 * @see CommandHandler
 */
interface AsyncCommandHandler<TCommand : Command> {
    /**
     * Handles a command
     *
     * @param command the command to handle
     */
    suspend fun handleAsync(command: TCommand)
}