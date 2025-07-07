@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.testing

import com.trendyol.kediatr.*
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

typealias MediatorAccessor = () -> Mediator

abstract class EnrichedWithMetadata {
  private val metadata = ConcurrentHashMap<String, Any>()

  internal fun incrementInvocationCount() {
    val invocationCount = invocationCount().incrementAndGet()
    addMetadata(INVOCATION_COUNT, AtomicInteger(invocationCount))
  }

  fun invocationCount(): AtomicInteger = getMetadata(INVOCATION_COUNT) as? AtomicInteger ?: AtomicInteger(0)

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

  internal fun addOrderedPipeline(pipeline: String) {
    val visitedPipelines = orderedVisitedPipelines().toMutableList()
    visitedPipelines.add(pipeline)
    addMetadata(ORDERED_VISITED_PIPELINES, visitedPipelines)
  }

  fun orderedVisitedPipelines(): List<String> = getMetadata(ORDERED_VISITED_PIPELINES) as? List<String> ?: emptyList()

  internal fun recordExecutionTime(time: Long) {
    addMetadata("executionTime", time)
  }

  fun executionTime(): Long = getMetadata("executionTime") as? Long ?: 0L

  internal fun recordThreadId(threadId: Long) {
    val threadIds = threadIds().toMutableSet()
    threadIds.add(threadId)
    addMetadata("threadIds", threadIds)
  }

  fun threadIds(): Set<Long> = getMetadata("threadIds") as? Set<Long> ?: emptySet()

  private fun addMetadata(key: String, value: Any) {
    metadata[key] = value
  }

  private fun getMetadata(key: String): Any? = metadata[key]

  companion object {
    private const val INVOCATION_COUNT = "invocationCount"
    private const val VISITED_PIPELINES = "visitedPipelines"
    private const val ORDERED_VISITED_PIPELINES = "orderedVisitedPipelines"
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

// Null Value Handling
class RequestWithNullableResult : Request<String?>

class RequestWithNullableResultHandler : RequestHandler<RequestWithNullableResult, String?> {
  override suspend fun handle(request: RequestWithNullableResult): String? = null
}

class RequestWithNullParameter(
  val nullValue: String?
) : EnrichedWithMetadata(),
  Request<String>

class RequestWithNullParameterHandler : RequestHandler<RequestWithNullParameter, String> {
  override suspend fun handle(request: RequestWithNullParameter): String {
    request.incrementInvocationCount()
    return request.nullValue ?: "null-handled"
  }
}

// Complex Generic Scenarios
class NestedGenericRequest<T, U>(
  val outer: T,
  val inner: U
) : EnrichedWithMetadata(),
  Request<Pair<T, U>>

class NestedGenericRequestHandler<T, U> : RequestHandler<NestedGenericRequest<T, U>, Pair<T, U>> {
  override suspend fun handle(request: NestedGenericRequest<T, U>): Pair<T, U> {
    request.incrementInvocationCount()
    return Pair(request.outer, request.inner)
  }
}

// Wildcard Generic Handling
class WildcardGenericRequest<T>(
  val value: T
) : EnrichedWithMetadata(),
  Request<Double>

class WildcardGenericRequestHandler<T> : RequestHandler<WildcardGenericRequest<T>, Double> {
  override suspend fun handle(request: WildcardGenericRequest<T>): Double {
    request.incrementInvocationCount()
    return request.value.toString().toDouble()
  }
}

// Concurrent Execution Testing
class ConcurrentRequest(
  val id: Int
) : EnrichedWithMetadata(),
  Request<String>

class ConcurrentRequestHandler : RequestHandler<ConcurrentRequest, String> {
  companion object {
    val executionCounter = AtomicInteger(0)
  }

  override suspend fun handle(request: ConcurrentRequest): String {
    request.incrementInvocationCount()
    request.recordThreadId(Thread.currentThread().id)
    val execution = executionCounter.incrementAndGet()
    delay(10) // Simulate some work
    return "concurrent-${request.id}-execution-$execution"
  }
}

// Long Running Request
class LongRunningRequest(
  val duration: Long
) : EnrichedWithMetadata(),
  Request<String>

class LongRunningRequestHandler : RequestHandler<LongRunningRequest, String> {
  override suspend fun handle(request: LongRunningRequest): String {
    request.incrementInvocationCount()
    val startTime = System.currentTimeMillis()
    delay(request.duration)
    val endTime = System.currentTimeMillis()
    request.recordExecutionTime(endTime - startTime)
    return "completed-after-${request.duration}ms"
  }
}

// Self-Referencing Request
class SelfReferencingRequest(
  val depth: Int
) : EnrichedWithMetadata(),
  Request<Int>

class SelfReferencingRequestHandler(
  private val mediator: MediatorAccessor
) : RequestHandler<SelfReferencingRequest, Int> {
  override suspend fun handle(request: SelfReferencingRequest): Int {
    request.incrementInvocationCount()
    return if (request.depth > 0) {
      val nextRequest = SelfReferencingRequest(request.depth - 1)
      mediator().send(nextRequest) + 1
    } else {
      0
    }
  }
}

// Multiple Interface Implementation
interface FirstMarker

interface SecondMarker

class MultiInterfaceRequest :
  EnrichedWithMetadata(),
  Request.Unit,
  FirstMarker,
  SecondMarker

class MultiInterfaceRequestHandler : RequestHandler.Unit<MultiInterfaceRequest> {
  override suspend fun handle(request: MultiInterfaceRequest) {
    request.incrementInvocationCount()
  }
}

// Empty Request/Response
class EmptyRequest : Request.Unit

class EmptyRequestHandler : RequestHandler.Unit<EmptyRequest> {
  override suspend fun handle(request: EmptyRequest) {
    // Intentionally empty
  }
}

class VoidResultRequest : Request<Unit>

class VoidResultRequestHandler : RequestHandler<VoidResultRequest, Unit> {
  override suspend fun handle(request: VoidResultRequest) {
    // Returns Unit explicitly
  }
}

// Collection Handling
class CollectionRequest(
  val items: List<String>
) : EnrichedWithMetadata(),
  Request<Set<String>>

class CollectionRequestHandler : RequestHandler<CollectionRequest, Set<String>> {
  override suspend fun handle(request: CollectionRequest): Set<String> {
    request.incrementInvocationCount()
    return request.items.toSet()
  }
}

// Exception Scenarios
class RequestThatThrowsSpecificException :
  EnrichedWithMetadata(),
  Request<String>

class RequestThatThrowsSpecificExceptionHandler : RequestHandler<RequestThatThrowsSpecificException, String> {
  override suspend fun handle(request: RequestThatThrowsSpecificException): String {
    request.incrementInvocationCount()
    throw IllegalArgumentException("Specific exception for testing")
  }
}

class RequestThatThrowsRuntimeException :
  EnrichedWithMetadata(),
  Request<String>

class RequestThatThrowsRuntimeExceptionHandler : RequestHandler<RequestThatThrowsRuntimeException, String> {
  override suspend fun handle(request: RequestThatThrowsRuntimeException): String {
    request.incrementInvocationCount()
    throw RuntimeException("Runtime exception for testing")
  }
}

// Notification Edge Cases
class NotificationWithoutHandlers :
  EnrichedWithMetadata(),
  Notification

class NotificationThatThrowsException :
  EnrichedWithMetadata(),
  Notification

class NotificationThatThrowsExceptionHandler1 : NotificationHandler<NotificationThatThrowsException> {
  override suspend fun handle(notification: NotificationThatThrowsException) {
    notification.incrementInvocationCount()
    throw RuntimeException("Handler 1 exception")
  }
}

class NotificationThatThrowsExceptionHandler2 : NotificationHandler<NotificationThatThrowsException> {
  override suspend fun handle(notification: NotificationThatThrowsException) {
    notification.incrementInvocationCount()
    throw IllegalStateException("Handler 2 exception")
  }
}

class NotificationThatThrowsExceptionHandler3 : NotificationHandler<NotificationThatThrowsException> {
  override suspend fun handle(notification: NotificationThatThrowsException) {
    notification.incrementInvocationCount()
    // This one succeeds
  }
}

// Slow Notification Handlers
class SlowNotification(
  val handlerDelay: Long
) : EnrichedWithMetadata(),
  Notification

class SlowNotificationHandler1 : NotificationHandler<SlowNotification> {
  override suspend fun handle(notification: SlowNotification) {
    notification.incrementInvocationCount()
    notification.recordThreadId(Thread.currentThread().id)
    val startTime = System.currentTimeMillis()
    delay(notification.handlerDelay)
    val endTime = System.currentTimeMillis()
    notification.recordExecutionTime(endTime - startTime)
  }
}

class SlowNotificationHandler2 : NotificationHandler<SlowNotification> {
  override suspend fun handle(notification: SlowNotification) {
    notification.incrementInvocationCount()
    notification.recordThreadId(Thread.currentThread().id)
    delay(notification.handlerDelay / 2)
  }
}

class SlowNotificationHandler3 : NotificationHandler<SlowNotification> {
  override suspend fun handle(notification: SlowNotification) {
    notification.incrementInvocationCount()
    notification.recordThreadId(Thread.currentThread().id)
    delay(notification.handlerDelay * 2)
  }
}

// Complex Pipeline Behavior Testing
interface CanPassComplexPipeline

class ComplexPipelineRequest :
  EnrichedWithMetadata(),
  Request<String>,
  CanPassComplexPipeline

class ComplexPipelineRequestHandler : RequestHandler<ComplexPipelineRequest, String> {
  override suspend fun handle(request: ComplexPipelineRequest): String {
    request.incrementInvocationCount()
    return "processed"
  }
}

class ModifyingPipelineBehavior : PipelineBehavior {
  override val order: Int = 100

  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    when (request) {
      is CanPassComplexPipeline -> {
        request as EnrichedWithMetadata
        request.addOrderedPipeline(this::class.java.simpleName)
      }
    }
    val result = next(request)

    // Modify response if it's a string
    return if (result is String) {
      "$result-modified" as TResponse
    } else {
      result
    }
  }
}

class ConditionalPipelineBehavior : PipelineBehavior {
  override val order: Int = 50

  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = when (request) {
    is CanPassComplexPipeline -> {
      request as EnrichedWithMetadata
      request.addOrderedPipeline(this::class.java.simpleName)
      // Skip processing for specific conditions
      if (request is ComplexPipelineRequest) {
        next(request)
      } else {
        next(request)
      }
    }

    else -> next(request)
  }
}

class TimingPipelineBehavior : PipelineBehavior {
  override val order: Int = 1

  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    val startTime = System.currentTimeMillis()

    when (request) {
      is CanPassComplexPipeline -> {
        request as EnrichedWithMetadata
        request.addOrderedPipeline(this::class.java.simpleName)
      }
    }

    val result = next(request)

    when (request) {
      is EnrichedWithMetadata -> {
        val endTime = System.currentTimeMillis()
        request.recordExecutionTime(endTime - startTime)
      }
    }

    return result
  }
}

// Data Classes with Complex Properties
data class ComplexDataRequest(
  val id: Long,
  val name: String,
  val metadata: Map<String, Any>,
  val tags: Set<String>,
  val nested: NestedData
) : EnrichedWithMetadata(),
  Request<ComplexDataResponse>

data class NestedData(
  val value: String,
  val count: Int
)

data class ComplexDataResponse(
  val processedId: Long,
  val processedName: String,
  val processedMetadata: Map<String, Any>,
  val processedTags: Set<String>,
  val processedNested: NestedData
)

class ComplexDataRequestHandler : RequestHandler<ComplexDataRequest, ComplexDataResponse> {
  override suspend fun handle(request: ComplexDataRequest): ComplexDataResponse {
    request.incrementInvocationCount()
    return ComplexDataResponse(
      processedId = request.id * 2,
      processedName = "processed-${request.name}",
      processedMetadata = request.metadata + ("processed" to true),
      processedTags = request.tags + "processed",
      processedNested = request.nested.copy(value = "processed-${request.nested.value}")
    )
  }
}
