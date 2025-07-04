package com.trendyol.kediatr

/**
 * Interface to be implemented for a non-blocking notification handler.
 *
 * Unlike query and command handlers, multiple notification handlers can be registered
 * for the same notification type. Notifications follow a fire-and-forget pattern where
 * the publisher doesn't expect a response, making them ideal for implementing:
 * - Event processing
 * - Logging and auditing
 * - Cache invalidation
 * - External system integration
 * - Side effects and reactions to domain events
 *
 * The execution behavior of multiple handlers depends on the chosen PublishStrategy.
 *
 * @param T The type of notification that extends Notification
 * @see Notification
 * @see PublishStrategy
 * @see Mediator.publish
 */
interface NotificationHandler<in T> where T : Notification {
  /**
   * Handles a notification.
   *
   * This method is called when a notification of type T is published through the mediator.
   * The handler should process the notification and perform any necessary side effects.
   * Since notifications are fire-and-forget, this method doesn't return a value.
   *
   * Example implementation:
   * ```kotlin
   * class OrderCreatedHandler : NotificationHandler<OrderCreated> {
   *     override suspend fun handle(notification: OrderCreated) {
   *         // Send confirmation email
   *         emailService.sendOrderConfirmation(notification.orderId)
   *
   *         // Log the event
   *         logger.info("Order created: ${notification.orderId}")
   *     }
   * }
   * ```
   *
   * @param notification The notification instance to handle
   * @throws Exception Any exception thrown during notification processing.
   *                   The behavior depends on the PublishStrategy used.
   */
  suspend fun handle(notification: T)
}
