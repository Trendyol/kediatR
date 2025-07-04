package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking command handler.
 *
 * Commands represent operations that modify state and may return results. Unlike queries,
 * commands are intended to perform actions that have side effects on the system state.
 * Each command type should have exactly one handler that processes it.
 *
 * Command handlers are executed through the mediator pipeline, which means they benefit
 * from pipeline behaviors for cross-cutting concerns like logging, validation, transactions, etc.
 *
 * Example implementation:
 * ```kotlin
 * data class CreateUserCommand(
 *     val name: String,
 *     val email: String
 * ) : Command<User>
 *
 * class CreateUserCommandHandler(
 *     private val userRepository: UserRepository
 * ) : CommandHandler<CreateUserCommand, User> {
 *     override suspend fun handle(command: CreateUserCommand): User {
 *         val user = User(name = command.name, email = command.email)
 *         return userRepository.save(user)
 *     }
 * }
 * ```
 *
 * @param TCommand The type of command that extends Command<TResult>
 * @param TResult The type of result that this handler will return
 * @see Command
 * @see Mediator.send
 * @see PipelineBehavior
 */
interface CommandHandler<TCommand : Command<TResult>, TResult> {
  /**
   * Handles a command and returns the result.
   *
   * This method contains the core business logic for processing the command.
   * It should perform the necessary operations to fulfill the command's intent
   * and return an appropriate result.
   *
   * The method is called by the mediator after all pipeline behaviors have
   * been executed in the pre-processing phase.
   *
   * @param command The command instance to handle
   * @return The result of the command execution
   * @throws Exception Any exception that occurs during command processing.
   *                   Exceptions will bubble up through the pipeline behaviors.
   */
  suspend fun handle(command: TCommand): TResult

  /**
   * Marker interface for a command handler that does not return a result.
   *
   * This is a specialized handler for commands that return Unit, providing
   * a more convenient way to implement handlers for fire-and-forget commands
   * that don't need to return data.
   *
   * Example implementation:
   * ```kotlin
   * data class DeleteUserCommand(val userId: Long) : Command.Unit
   *
   * class DeleteUserCommandHandler : CommandHandler.Unit<DeleteUserCommand> {
   *     override suspend fun handle(command: DeleteUserCommand) {
   *         userRepository.delete(command.userId)
   *         // No return value needed
   *     }
   * }
   * ```
   *
   * @param TCommand The type of command that extends Command.Unit
   * @see Command.Unit
   */
  interface Unit<TCommand : Command.Unit> : CommandHandler<TCommand, kotlin.Unit>
}
