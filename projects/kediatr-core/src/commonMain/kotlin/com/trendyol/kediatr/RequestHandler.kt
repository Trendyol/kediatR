package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking request handler.
 *
 * Requests represent both queries and commands in a unified way, allowing for consistent
 * handling mechanisms across different types of operations. Unlike the previous separate
 * CommandHandler and QueryHandler interfaces, RequestHandler provides a single interface
 * for handling all request types.
 *
 * Request handlers are executed through the mediator pipeline, which means they benefit
 * from pipeline behaviors for cross-cutting concerns like logging, validation, transactions, etc.
 *
 * Example implementations:
 * ```kotlin
 * // Command that returns a result
 * data class CreateUserCommand(
 *     val name: String,
 *     val email: String
 * ) : Request<User>
 *
 * class CreateUserCommandHandler(
 *     private val userRepository: UserRepository
 * ) : RequestHandler<CreateUserCommand, User> {
 *     override suspend fun handle(request: CreateUserCommand): User {
 *         val user = User(name = request.name, email = request.email)
 *         return userRepository.save(user)
 *     }
 * }
 *
 * // Query example
 * data class GetUserQuery(val userId: Long) : Request<User>
 *
 * class GetUserQueryHandler(
 *     private val userRepository: UserRepository
 * ) : RequestHandler<GetUserQuery, User> {
 *     override suspend fun handle(request: GetUserQuery): User {
 *         return userRepository.findById(request.userId)
 *     }
 * }
 * ```
 *
 * @param TRequest The type of request that extends Request<TResult>
 * @param TResult The type of result that this handler will return
 * @see Request
 * @see Mediator.send
 * @see PipelineBehavior
 */
interface RequestHandler<TRequest : Request<TResult>, TResult> {
  /**
   * Handles a request and returns the result.
   *
   * This method contains the core business logic for processing the request.
   * It should perform the necessary operations to fulfill the request's intent
   * and return an appropriate result.
   *
   * The method is called by the mediator after all pipeline behaviors have
   * been executed in the pre-processing phase.
   *
   * @param request The request instance to handle
   * @return The result of the request execution
   * @throws Exception Any exception that occurs during request processing.
   *                   Exceptions will bubble up through the pipeline behaviors.
   */
  suspend fun handle(request: TRequest): TResult

  /**
   * Marker interface for a request handler that does not return a result.
   *
   * This is a specialized handler for requests that return Unit, providing
   * a more convenient way to implement handlers for fire-and-forget requests
   * that don't need to return data.
   *
   * Example implementation:
   * ```kotlin
   * data class DeleteUserCommand(val userId: Long) : Request.Unit
   *
   * class DeleteUserCommandHandler : RequestHandler.Unit<DeleteUserCommand> {
   *     override suspend fun handle(request: DeleteUserCommand) {
   *         userRepository.delete(request.userId)
   *         // No return value needed
   *     }
   * }
   * ```
   *
   * @param TRequest The type of request that extends Request.Unit
   * @see Request.Unit
   */
  interface Unit<TRequest : Request.Unit> : RequestHandler<TRequest, kotlin.Unit>
}
