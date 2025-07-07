@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.testing

import com.trendyol.kediatr.*
import io.kotest.matchers.shouldNotBe

typealias MediatorAccessor = () -> Mediator

abstract class EnrichedWithMetadata {
  private val metadata = mutableMapOf<String, Any>()

  internal fun incrementInvocationCount() {
    val invocationCount = invocationCount()
    addMetadata(INVOCATION_COUNT, invocationCount + 1)
  }

  fun invocationCount(): Int = getMetadata(INVOCATION_COUNT) as? Int ?: 0

  fun whereItWasInvokedFrom(): String = getMetadata("invokedFrom") as? String ?: "unknown"

  fun invokedFrom(nameOfTheHandler: String) {
    addMetadata("invokedFrom", nameOfTheHandler)
  }

  internal fun visitedPipeline(pipeline: String) {
    val visitedPipelines = visitedPipelines().toMutableSet()
    visitedPipelines.add(pipeline)
    addMetadata(VISITED_PIPELINES, visitedPipelines)
  }

  fun visitedPipelines(): Set<String> = getMetadata(VISITED_PIPELINES) as? Set<String> ?: emptySet()

  private fun addMetadata(key: String, value: Any) {
    metadata[key] = value
  }

  private fun getMetadata(key: String): Any? = metadata[key]

  companion object {
    private const val INVOCATION_COUNT = "invocationCount"
    private const val VISITED_PIPELINES = "visitedPipelines"
  }
}

class Result(
  val value: Int = 0
)

class TestNonExistCommand : Request.Unit

class NonExistCommandWithResult : Request.Unit

class NonExistQuery : Request<String>

/**
 * Notifications
 */

class TestNotification :
  EnrichedWithMetadata(),
  Notification

class TestNotificationHandler(
  private val mediator: MediatorAccessor
) : NotificationHandler<TestNotification> {
  override suspend fun handle(notification: TestNotification) {
    mediator shouldNotBe null
    notification.incrementInvocationCount()
  }
}

open class Ping :
  EnrichedWithMetadata(),
  Notification

class ExtendedPing : Ping()

class APingHandler : NotificationHandler<ExtendedPing> {
  override suspend fun handle(notification: ExtendedPing) {
    notification.incrementInvocationCount()
  }
}

class AnotherPingHandler : NotificationHandler<Ping> {
  override suspend fun handle(notification: Ping) {
    notification.incrementInvocationCount()
  }
}

class NotificationForMultipleHandlers :
  EnrichedWithMetadata(),
  Notification

class Handler1ForNotificationOfMultipleHandlers : NotificationHandler<NotificationForMultipleHandlers> {
  override suspend fun handle(notification: NotificationForMultipleHandlers) {
    notification.incrementInvocationCount()
  }
}

class Handler2ForNotificationOfMultipleHandlers : NotificationHandler<NotificationForMultipleHandlers> {
  override suspend fun handle(notification: NotificationForMultipleHandlers) {
    notification.incrementInvocationCount()
  }
}

class PingForInherited :
  EnrichedWithMetadata(),
  Notification

abstract class NotificationHandlerBase<TNotification : Notification> : NotificationHandler<TNotification>

class InheritedNotificationHandler : NotificationHandlerBase<PingForInherited>() {
  override suspend fun handle(notification: PingForInherited) {
    notification.incrementInvocationCount()
  }
}

class ParameterizedNotification<T>(
  val param: T
) : EnrichedWithMetadata(),
  Notification

class ParameterizedNotificationHandler<A> : NotificationHandler<ParameterizedNotification<A>> {
  override suspend fun handle(notification: ParameterizedNotification<A>) {
    notification.param shouldNotBe null
    notification.incrementInvocationCount()
  }
}

class ParameterizedNotificationForInheritance<T>(
  val param: T
) : EnrichedWithMetadata(),
  Notification

class ParameterizedNotificationHandlerForInheritance<T> : NotificationHandlerBase<ParameterizedNotificationForInheritance<T>>() {
  override suspend fun handle(notification: ParameterizedNotificationForInheritance<T>) {
    notification.param shouldNotBe null
    notification.incrementInvocationCount()
  }
}

/**
 * Commands
 */

class TestCommandForWithoutInjection :
  EnrichedWithMetadata(),
  Request.Unit

class TestRequestHandlerWithoutInjection : RequestHandler.Unit<TestCommandForWithoutInjection> {
  override suspend fun handle(request: TestCommandForWithoutInjection) {
    request.incrementInvocationCount()
  }
}

class TestCommand :
  EnrichedWithMetadata(),
  Request.Unit

class TestRequestHandler(
  private val mediator: MediatorAccessor
) : RequestHandler.Unit<TestCommand> {
  override suspend fun handle(request: TestCommand) {
    mediator() shouldNotBe null
    request.incrementInvocationCount()
  }
}

data class TestCommandWithResult(
  val invoked: Int = 0
) : EnrichedWithMetadata(),
  Request<Result>

class TestCommandWithResultRequestHandler(
  val mediator: MediatorAccessor
) : RequestHandler<TestCommandWithResult, Result> {
  override suspend fun handle(
    request: TestCommandWithResult
  ): Result = Result(request.invoked + 1).also {
    mediator() shouldNotBe null
    request.incrementInvocationCount()
  }
}

class CommandThatPassesThroughPipelineBehaviours :
  EnrichedWithMetadata(),
  Request.Unit,
  CanPassLoggingPipelineBehaviour,
  CanPassExceptionPipelineBehaviour,
  CanPassInheritedPipelineBehaviour

class TestPipelineRequestHandler(
  private val mediator: MediatorAccessor
) : RequestHandler.Unit<CommandThatPassesThroughPipelineBehaviours> {
  override suspend fun handle(request: CommandThatPassesThroughPipelineBehaviours) {
    mediator shouldNotBe null
    request.incrementInvocationCount()
  }
}

class CommandForWithoutInjectionThatPassesThroughPipelineBehaviours :
  EnrichedWithMetadata(),
  Request.Unit,
  CanPassLoggingPipelineBehaviour,
  CanPassExceptionPipelineBehaviour,
  CanPassInheritedPipelineBehaviour

class TestPipelineRequestHandlerWithoutInjection : RequestHandler.Unit<CommandForWithoutInjectionThatPassesThroughPipelineBehaviours> {
  override suspend fun handle(request: CommandForWithoutInjectionThatPassesThroughPipelineBehaviours) {
    request.incrementInvocationCount()
  }
}

class CommandThatFailsWhilePassingThroughPipelineBehaviours :
  EnrichedWithMetadata(),
  Request.Unit

class TestPipelineRequestHandlerThatFails : RequestHandler.Unit<CommandThatFailsWhilePassingThroughPipelineBehaviours> {
  override suspend fun handle(request: CommandThatFailsWhilePassingThroughPipelineBehaviours) {
    request.incrementInvocationCount()
    throw Exception()
  }
}

class TestCommandThatFailsWithException :
  EnrichedWithMetadata(),
  Request.Unit

class TestBrokenRequestHandler(
  private val mediator: MediatorAccessor
) : RequestHandler.Unit<TestCommandThatFailsWithException> {
  override suspend fun handle(request: TestCommandThatFailsWithException) {
    mediator shouldNotBe null
    request.incrementInvocationCount()
    throw Exception()
  }
}

class TestCommandForInheritance :
  EnrichedWithMetadata(),
  Request.Unit

abstract class MyRequestHandlerBaseForSpecificCommand : RequestHandler.Unit<TestCommandForInheritance>

class TestInheritedRequestHandlerForSpecificCommand : MyRequestHandlerBaseForSpecificCommand() {
  override suspend fun handle(request: TestCommandForInheritance) {
    request.incrementInvocationCount()
  }
}

class TestCommandForTypeLimitedInheritance :
  EnrichedWithMetadata(),
  Request.Unit

abstract class TestBaseRequestHandlerForTypeLimitedInheritance<TCommand : Request.Unit> : RequestHandler.Unit<TCommand>

class TestRequestHandlerForTypeLimitedInheritance :
  TestBaseRequestHandlerForTypeLimitedInheritance<TestCommandForTypeLimitedInheritance>() {
  override suspend fun handle(request: TestCommandForTypeLimitedInheritance) {
    request.incrementInvocationCount()
  }
}

class ParameterizedCommand<T>(
  val param: T
) : EnrichedWithMetadata(),
  Request.Unit

class ParameterizedRequestHandler<T> : RequestHandler.Unit<ParameterizedCommand<T>> {
  override suspend fun handle(request: ParameterizedCommand<T>) {
    request.param shouldNotBe null
    request.incrementInvocationCount()
  }
}

class ParameterizedCommandForInheritedRequestHandler<T>(
  val param: T
) : EnrichedWithMetadata(),
  Request.Unit

abstract class ParameterizedRequestHandlerBaseForInheritedRequestHandler<A> :
  RequestHandler.Unit<ParameterizedCommandForInheritedRequestHandler<A>>

class ParameterizedRequestHandlerForInheritance<A> : ParameterizedRequestHandlerBaseForInheritedRequestHandler<A>() {
  override suspend fun handle(request: ParameterizedCommandForInheritedRequestHandler<A>) {
    request.param shouldNotBe null
    request.incrementInvocationCount()
  }
}

class ParameterizedCommandWithResult<TParam, TReturn>(
  val param: TParam,
  val retFn: suspend (TParam) -> TReturn
) : EnrichedWithMetadata(),
  Request<TReturn>

class ParameterizedCommandWithResultHandler<TParam, TReturn> : RequestHandler<ParameterizedCommandWithResult<TParam, TReturn>, TReturn> {
  override suspend fun handle(request: ParameterizedCommandWithResult<TParam, TReturn>): TReturn {
    request.param shouldNotBe null
    request.incrementInvocationCount()
    return request.retFn(request.param)
  }
}

data class ParameterizedCommandWithResultForInheritance<TParam>(
  val param: TParam
) : EnrichedWithMetadata(),
  Request<String>

abstract class ParameterizedCommandWithResultHandlerBase<TParam : Request<String>> : RequestHandler<TParam, String>

class ParameterizedCommandWithResultHandlerOfInheritedHandler<TParam> :
  ParameterizedCommandWithResultHandlerBase<ParameterizedCommandWithResultForInheritance<TParam>>() {
  override suspend fun handle(request: ParameterizedCommandWithResultForInheritance<TParam>): String {
    request.param shouldNotBe null
    request.incrementInvocationCount()
    return request.param.toString()
  }
}

/**
 * Queries
 */

class TestQuery(
  val id: Int
) : EnrichedWithMetadata(),
  Request<String>

class TestQueryHandler(
  private val mediator: MediatorAccessor
) : RequestHandler<TestQuery, String> {
  override suspend fun handle(request: TestQuery): String {
    mediator shouldNotBe null
    request.incrementInvocationCount()
    return "hello " + request.id
  }
}

class ParameterizedQuery<TParam, TResponse>(
  val param: TParam,
  val retFn: suspend (TParam) -> TResponse
) : EnrichedWithMetadata(),
  Request<TResponse>

class ParameterizedQueryHandler<TParam, TResponse> : RequestHandler<ParameterizedQuery<TParam, TResponse>, TResponse> {
  override suspend fun handle(request: ParameterizedQuery<TParam, TResponse>): TResponse {
    request.param shouldNotBe null
    request.incrementInvocationCount()
    return request.retFn(request.param)
  }
}

/**
 * Pipeline Behaviors
 */
interface CanPassExceptionPipelineBehaviour

class ExceptionPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = try {
    when (request) {
      is CanPassExceptionPipelineBehaviour -> {
        request as EnrichedWithMetadata
        request.visitedPipeline(this::class.java.simpleName)
      }
    }
    next(request)
  } catch (ex: Exception) {
    throw ex
  }
}

interface CanPassLoggingPipelineBehaviour

class LoggingPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    when (request) {
      is CanPassLoggingPipelineBehaviour -> {
        request as EnrichedWithMetadata
        request.visitedPipeline(this::class.java.simpleName)
      }
    }
    return next(request)
  }
}

abstract class MyBasePipelineBehaviour : PipelineBehavior

interface CanPassInheritedPipelineBehaviour

class InheritedPipelineBehaviour : MyBasePipelineBehaviour() {
  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    when (request) {
      is CanPassInheritedPipelineBehaviour -> {
        request as EnrichedWithMetadata
        request.visitedPipeline(this::class.java.simpleName)
      }
    }
    return next(request)
  }
}

interface OrderedPipelineUseCase

class CommandThatPassesThroughOrderedPipelineBehaviours :
  EnrichedWithMetadata(),
  Request.Unit,
  OrderedPipelineUseCase

class QueryThatPassesThroughOrderedPipelineBehaviours :
  EnrichedWithMetadata(),
  Request<String>,
  OrderedPipelineUseCase

class NotificationThatPassesThroughOrderedPipelineBehaviours :
  EnrichedWithMetadata(),
  Notification,
  OrderedPipelineUseCase

class RequestHandlerThatPassesThroughOrderedPipelineBehaviours : RequestHandler.Unit<CommandThatPassesThroughOrderedPipelineBehaviours> {
  override suspend fun handle(request: CommandThatPassesThroughOrderedPipelineBehaviours) {
    request.incrementInvocationCount()
  }
}

class QueryHandlerThatPassesThroughOrderedPipelineBehaviours : RequestHandler<QueryThatPassesThroughOrderedPipelineBehaviours, String> {
  override suspend fun handle(request: QueryThatPassesThroughOrderedPipelineBehaviours): String {
    request.incrementInvocationCount()
    return "hello"
  }
}

class NotificationHandlerThatPassesThroughOrderedPipelineBehaviours :
  NotificationHandler<NotificationThatPassesThroughOrderedPipelineBehaviours> {
  override suspend fun handle(notification: NotificationThatPassesThroughOrderedPipelineBehaviours) {
    notification.incrementInvocationCount()
  }
}

class FirstPipelineBehaviour : PipelineBehavior {
  override val order: Int = 1

  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    when (request) {
      is OrderedPipelineUseCase -> {
        request as EnrichedWithMetadata
        request.visitedPipeline(this::class.java.simpleName)
      }
    }
    return next(request)
  }
}

class SecondPipelineBehaviour : PipelineBehavior {
  override val order: Int = 2

  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    when (request) {
      is OrderedPipelineUseCase -> {
        request as EnrichedWithMetadata
        request.visitedPipeline(this::class.java.simpleName)
      }
    }
    return next(request)
  }
}

class ThirdPipelineBehaviour : PipelineBehavior {
  override val order: Int = 3

  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    when (request) {
      is OrderedPipelineUseCase -> {
        request as EnrichedWithMetadata
        request.visitedPipeline(this::class.java.simpleName)
      }
    }
    return next(request)
  }
}

sealed class TestCommandBase :
  EnrichedWithMetadata(),
  Request.Unit {
  abstract val id: String

  data class TestCommandInherited1(
    override val id: String
  ) : TestCommandBase()

  data class TestCommandInherited2(
    override val id: String
  ) : TestCommandBase()
}

class TestCommandBaseHandler : RequestHandler.Unit<TestCommandBase> {
  override suspend fun handle(request: TestCommandBase) {
    request.incrementInvocationCount()
  }
}

sealed class TestQueryBase :
  EnrichedWithMetadata(),
  Request<String> {
  abstract val id: String

  data class TestQueryInherited1(
    override val id: String
  ) : TestQueryBase()

  data class TestQueryInherited2(
    override val id: String
  ) : TestQueryBase()
}

class TestQueryBaseHandler : RequestHandler<TestQueryBase, String> {
  override suspend fun handle(request: TestQueryBase): String {
    request.incrementInvocationCount()
    return request.id
  }
}

sealed class TestCommandWithResultBase :
  EnrichedWithMetadata(),
  Request<String> {
  abstract val id: String

  data class TestCommandWithResultInherited1(
    override val id: String
  ) : TestCommandWithResultBase()

  data class TestCommandWithResultInherited2(
    override val id: String
  ) : TestCommandWithResultBase()
}

class TestCommandWithResultBaseHandler : RequestHandler<TestCommandWithResultBase, String> {
  override suspend fun handle(request: TestCommandWithResultBase): String {
    request.incrementInvocationCount()
    return "${request.id} handled"
  }
}

sealed class TestCommandForInheritanceWithFallback :
  EnrichedWithMetadata(),
  Request.Unit {
  abstract val id: String

  data class TestCommandInherited1(
    override val id: String
  ) : TestCommandForInheritanceWithFallback()

  data class TestCommandInherited2(
    override val id: String
  ) : TestCommandForInheritanceWithFallback()
}

class TestCommandForInheritanceWithFallbackHandlerHandler : RequestHandler.Unit<TestCommandForInheritanceWithFallback> {
  override suspend fun handle(request: TestCommandForInheritanceWithFallback) {
    request.incrementInvocationCount()
    request.invokedFrom(javaClass.name)
  }
}

class TestRequestHandlerForCommandInherited2 : RequestHandler.Unit<TestCommandForInheritanceWithFallback.TestCommandInherited2> {
  override suspend fun handle(request: TestCommandForInheritanceWithFallback.TestCommandInherited2) {
    request.incrementInvocationCount()
    request.invokedFrom(javaClass.name)
  }
}
