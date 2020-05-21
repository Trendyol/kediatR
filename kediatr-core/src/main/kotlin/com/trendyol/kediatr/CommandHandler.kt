package com.trendyol.kediatr

/**
 * Interface to be implemented for a  command handler
 *
 * @since 1.0.0
 * @param TCommand any [Command] subclass to handle
 * @see Command
 * @see AsyncCommandHandler
 */
interface CommandHandler<TCommand : Command> {
    /**
     * Handles a command
     *
     * @param command the command to handle
     */
    fun handle(command: TCommand)
}