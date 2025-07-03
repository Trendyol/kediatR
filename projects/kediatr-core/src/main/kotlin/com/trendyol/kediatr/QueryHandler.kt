package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking query handler.
 *
 * Queries represent read operations that return data without modifying system state.
 * They follow the Command Query Responsibility Segregation (CQRS) principle where
 * queries are purely for data retrieval and should not have side effects.
 * Each query type should have exactly one handler that processes it.
 *
 * Query handlers are executed through the mediator pipeline, which means they benefit
 * from pipeline behaviors for cross-cutting concerns like logging, caching, validation, etc.
 *
 * Example implementation:
 * ```kotlin
 * data class GetUserQuery(val userId: Long) : Query<User>
 *
 * class GetUserQueryHandler(
 *     private val userRepository: UserRepository
 * ) : QueryHandler<GetUserQuery, User> {
 *     override suspend fun handle(query: GetUserQuery): User {
 *         return userRepository.findById(query.userId)
 *             ?: throw UserNotFoundException("User not found: ${query.userId}")
 *     }
 * }
 * ```
 *
 * @param TQuery The type of query that extends Query<TResponse>
 * @param TResponse The type of response that this handler will return
 * @see Query
 * @see Mediator.send
 * @see PipelineBehavior
 */
interface QueryHandler<TQuery : Query<TResponse>, TResponse> {
  /**
   * Handles a query and returns the response.
   *
   * This method contains the logic for retrieving and returning the requested data.
   * It should be idempotent and not modify system state. The method is called by
   * the mediator after all pipeline behaviors have been executed in the pre-processing phase.
   *
   * Best practices:
   * - Keep the handler focused on a single responsibility
   * - Avoid side effects or state modifications
   * - Use appropriate exception handling for error scenarios
   * - Consider performance implications for data access patterns
   *
   * @param query The query instance containing the parameters for data retrieval
   * @return The response data as specified by the query contract
   * @throws Exception Any exception that occurs during query processing.
   *                   Exceptions will bubble up through the pipeline behaviors.
   */
  suspend fun handle(query: TQuery): TResponse
}
