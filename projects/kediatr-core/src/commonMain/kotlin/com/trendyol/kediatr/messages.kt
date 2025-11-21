package com.trendyol.kediatr

/**
 * Marker interface for a message in the Kediatr framework.
 *
 * This is the base interface for all messages that can be processed through the mediator.
 * It serves as a common contract for requests and notifications, enabling
 * type-safe message handling and polymorphic processing.
 *
 * The Message interface follows the marker interface pattern, providing no methods
 * but establishing a type hierarchy that enables the mediator to route different
 * types of messages to their appropriate handlers.
 *
 * @see Request
 * @see Notification
 * @see Mediator
 */
sealed interface Message

/**
 * Marker interface for a request in the CQRS pattern.
 *
 * Requests are the base type for all messages that can be sent through the mediator.
 * They represent both queries and commands, allowing for a unified handling mechanism.
 * This interface is used to define the contract for messages that expect a response.
 *
 * @param TResponse The type of response data that this request will return

 * @see Mediator.send
 */
interface Request<out TResponse> : Message {
  /**
   * Units are a special case of requests that do not return any response.
   */
  interface Unit : Request<kotlin.Unit>
}

/**
 * Marker interface for a notification in the publish-subscribe pattern.
 *
 * Notifications represent events or messages that can be published to multiple handlers.
 * Unlike requests, notifications follow a fire-and-forget pattern where
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
