package com.trendyol.kediatr

/**
 * Marker interface for a message in the Kediatr framework.
 *
 * This is the base interface for all messages that can be processed through the mediator.
 * It serves as a common contract for queries, commands, and notifications, enabling
 * type-safe message handling and polymorphic processing.
 *
 * The Message interface follows the marker interface pattern, providing no methods
 * but establishing a type hierarchy that enables the mediator to route different
 * types of messages to their appropriate handlers.
 *
 * @see Query
 * @see Command
 * @see Notification
 * @see Mediator
 */
sealed interface Message

/**
 * Marker interface for a query in the CQRS pattern.
 *
 * Queries represent read operations that return data without modifying system state.
 * They follow the Command Query Responsibility Segregation (CQRS) principle where
 * queries are purely for data retrieval and should not have side effects.
 *
 * Each query type should have exactly one handler that processes it, ensuring
 * a clear one-to-one mapping between queries and their processing logic.
 *
 * Example usage:
 * ```kotlin
 * data class GetUserQuery(val userId: Long) : Query<User>
 * data class GetOrdersQuery(val customerId: Long, val status: OrderStatus) : Query<List<Order>>
 * data class SearchProductsQuery(val term: String, val category: String?) : Query<PagedResult<Product>>
 * ```
 *
 * @param TResponse The type of response data that this query will return
 * @see QueryHandler
 * @see Mediator.send
 */
interface Query<TResponse> : Message

/**
 * Marker interface for a command in the CQRS pattern.
 *
 * Commands represent operations that modify system state and may return results.
 * Unlike queries, commands are intended to perform actions that have side effects,
 * such as creating, updating, or deleting data.
 *
 * Each command type should have exactly one handler that processes it, ensuring
 * a clear separation of concerns and maintainable code structure.
 *
 * Commands can return results (such as created entity IDs, success indicators, or
 * computed values), or they can be fire-and-forget operations that return Unit.
 *
 * Example usage:
 * ```kotlin
 * data class CreateUserCommand(val name: String, val email: String) : Command<User>
 * data class UpdateOrderStatusCommand(val orderId: Long, val status: OrderStatus) : Command<Order>
 * data class ProcessPaymentCommand(val paymentId: Long, val amount: BigDecimal) : Command<PaymentResult>
 * ```
 *
 * @param TResult The type of result that this command will return after processing
 * @see CommandHandler
 * @see Mediator.send
 */
interface Command<TResult> : Message {
  /**
   * Marker interface for a command that does not return a result.
   *
   * This specialized interface is for commands that perform actions but don't need
   * to return any data. These are typically fire-and-forget operations where the
   * caller only cares about successful completion, not about any return value.
   *
   * Using Command.Unit provides better semantics than Command<kotlin.Unit> and
   * allows for specialized handler implementations that don't need to worry about
   * return values.
   *
   * Example usage:
   * ```kotlin
   * data class DeleteUserCommand(val userId: Long) : Command.Unit
   * data class SendEmailCommand(val to: String, val subject: String, val body: String) : Command.Unit
   * data class LogAuditEventCommand(val userId: Long, val action: String) : Command.Unit
   * ```
   *
   * @see CommandHandler.Unit
   */
  interface Unit : Command<kotlin.Unit>
}

/**
 * Marker interface for a notification in the publish-subscribe pattern.
 *
 * Notifications represent events or messages that can be published to multiple handlers.
 * Unlike queries and commands, notifications follow a fire-and-forget pattern where
 * the publisher doesn't expect a response, and multiple handlers can process the
 * same notification.
 *
 * Notifications are ideal for implementing:
 * - Domain events and event sourcing
 * - Cross-cutting concerns like logging and auditing
 * - Integration with external systems
 * - Cache invalidation and data synchronization
 * - Decoupled communication between bounded contexts
 *
 * The execution behavior of multiple handlers depends on the chosen PublishStrategy:
 * - Sequential execution with different exception handling strategies
 * - Parallel execution with various completion patterns
 *
 * Example usage:
 * ```kotlin
 * data class UserCreatedNotification(val userId: Long, val email: String) : Notification
 * data class OrderCompletedNotification(val orderId: Long, val customerId: Long, val total: BigDecimal) : Notification
 * data class PaymentProcessedNotification(val paymentId: Long, val amount: BigDecimal, val status: PaymentStatus) : Notification
 * ```
 *
 * @see NotificationHandler
 * @see Mediator.publish
 * @see PublishStrategy
 */
interface Notification : Message
