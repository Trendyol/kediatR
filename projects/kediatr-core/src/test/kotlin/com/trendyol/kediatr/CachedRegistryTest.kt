@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr

import com.trendyol.kediatr.testing.*
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.*
import io.kotest.matchers.types.shouldBeSameInstanceAs

class CachedRegistryTest :
  ShouldSpec({

    context("CachedRegistry request handler caching") {
      should("cache request handlers and return same instance on subsequent calls") {
        // Arrange
        val testHandler = TestRequestHandlerWithoutInjection()
        val handlers = listOf(testHandler)
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act
        val firstCall = cachedRegistry.resolveHandler(TestCommandForWithoutInjection::class.java)
        val secondCall = cachedRegistry.resolveHandler(TestCommandForWithoutInjection::class.java)

        // Assert
        firstCall shouldBeSameInstanceAs secondCall
      }

      should("cache different handlers for different request types") {
        // Arrange
        val commandHandler = TestRequestHandlerWithoutInjection()
        val inheritedHandler = TestInheritedRequestHandlerForSpecificCommand()
        val handlers = listOf(commandHandler, inheritedHandler)
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act
        val resolvedCommandHandler = cachedRegistry.resolveHandler(TestCommandForWithoutInjection::class.java)
        val resolvedInheritedHandler = cachedRegistry.resolveHandler(TestCommandForInheritance::class.java)

        // Assert
        resolvedCommandHandler shouldNotBe resolvedInheritedHandler
      }

      should("propagate exceptions from underlying registry") {
        // Arrange - empty registry will throw HandlerNotFoundException
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(hashMapOf()))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act & Assert
        var exceptionCount = 0
        try {
          cachedRegistry.resolveHandler(TestCommandForWithoutInjection::class.java)
        } catch (e: HandlerNotFoundException) {
          exceptionCount++
        }

        // Verify exception is not cached - should throw again
        try {
          cachedRegistry.resolveHandler(TestCommandForWithoutInjection::class.java)
        } catch (e: HandlerNotFoundException) {
          exceptionCount++
        }

        exceptionCount shouldBe 2
      }
    }

    context("CachedRegistry notification handler caching") {
      should("cache notification handlers and return same collection on subsequent calls") {
        // Arrange
        val notificationHandler = Handler1ForNotificationOfMultipleHandlers()
        val handlers = listOf(notificationHandler)
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act
        val firstCall = cachedRegistry.resolveNotificationHandlers(NotificationForMultipleHandlers::class.java)
        val secondCall = cachedRegistry.resolveNotificationHandlers(NotificationForMultipleHandlers::class.java)

        // Assert
        firstCall shouldBeSameInstanceAs secondCall
      }

      should("cache empty collections for notifications with no handlers") {
        // Arrange - empty registry will return empty collection
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(hashMapOf()))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act
        val firstCall = cachedRegistry.resolveNotificationHandlers(TestNotification::class.java)
        val secondCall = cachedRegistry.resolveNotificationHandlers(TestNotification::class.java)

        // Assert
        firstCall shouldBeSameInstanceAs secondCall
        firstCall shouldBe emptyList()
      }

      should("cache multiple notification handlers correctly") {
        // Arrange
        val handler1 = Handler1ForNotificationOfMultipleHandlers()
        val handler2 = Handler2ForNotificationOfMultipleHandlers()
        val handlers = listOf(handler1, handler2)
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act
        val firstCall = cachedRegistry.resolveNotificationHandlers(NotificationForMultipleHandlers::class.java)
        val secondCall = cachedRegistry.resolveNotificationHandlers(NotificationForMultipleHandlers::class.java)

        // Assert
        firstCall shouldBeSameInstanceAs secondCall
        firstCall.size shouldBe 2
      }
    }

    context("CachedRegistry pipeline behavior caching") {
      should("cache pipeline behaviors and return same collection on subsequent calls") {
        // Arrange
        val pipelineBehavior = ExceptionPipelineBehavior()
        val handlers = listOf(pipelineBehavior)
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act
        val firstCall = cachedRegistry.getPipelineBehaviors()
        val secondCall = cachedRegistry.getPipelineBehaviors()

        // Assert
        firstCall shouldBeSameInstanceAs secondCall
      }

      should("cache multiple pipeline behaviors correctly") {
        // Arrange
        val behavior1 = ExceptionPipelineBehavior()
        val behavior2 = LoggingPipelineBehavior()
        val handlers = listOf(behavior1, behavior2)
        val baseRegistry = RegistryImpl(HandlerRegistryProvider(handlers.associateBy { it.javaClass } as HashMap<Class<*>, Any>))
        val cachedRegistry = CachedRegistry(baseRegistry)

        // Act
        val firstCall = cachedRegistry.getPipelineBehaviors()
        val secondCall = cachedRegistry.getPipelineBehaviors()

        // Assert
        firstCall shouldBeSameInstanceAs secondCall
        firstCall.size shouldBe 2
      }
    }
  })
