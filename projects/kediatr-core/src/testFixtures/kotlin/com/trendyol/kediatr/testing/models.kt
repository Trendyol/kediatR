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

class TestNonExistCommand : Command

class NonExistCommandWithResult : CommandWithResult<Result>

class NonExistQuery : Query<String>

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
  Command

class TestCommandHandlerWithoutInjection : CommandHandler<TestCommandForWithoutInjection> {
  override suspend fun handle(command: TestCommandForWithoutInjection) {
    command.incrementInvocationCount()
  }
}

class TestCommand :
  EnrichedWithMetadata(),
  Command

class TestCommandHandler(
  private val mediator: MediatorAccessor
) : CommandHandler<TestCommand> {
  override suspend fun handle(command: TestCommand) {
    mediator() shouldNotBe null
    command.incrementInvocationCount()
  }
}

data class TestCommandWithResult(
  val invoked: Int = 0
) : EnrichedWithMetadata(),
  CommandWithResult<Result>

class TestCommandWithResultCommandHandler(
  val mediator: MediatorAccessor
) : CommandWithResultHandler<TestCommandWithResult, Result> {
  override suspend fun handle(
    command: TestCommandWithResult
  ): Result = Result(command.invoked + 1).also {
    mediator() shouldNotBe null
    command.incrementInvocationCount()
  }
}

class CommandThatPassesThroughPipelineBehaviours :
  EnrichedWithMetadata(),
  Command,
  CanPassLoggingPipelineBehaviour,
  CanPassExceptionPipelineBehaviour,
  CanPassInheritedPipelineBehaviour

class TestPipelineCommandHandler(
  private val mediator: MediatorAccessor
) : CommandHandler<CommandThatPassesThroughPipelineBehaviours> {
  override suspend fun handle(command: CommandThatPassesThroughPipelineBehaviours) {
    mediator shouldNotBe null
    command.incrementInvocationCount()
  }
}

class CommandForWithoutInjectionThatPassesThroughPipelineBehaviours :
  EnrichedWithMetadata(),
  Command,
  CanPassLoggingPipelineBehaviour,
  CanPassExceptionPipelineBehaviour,
  CanPassInheritedPipelineBehaviour

class TestPipelineCommandHandlerWithoutInjection : CommandHandler<CommandForWithoutInjectionThatPassesThroughPipelineBehaviours> {
  override suspend fun handle(command: CommandForWithoutInjectionThatPassesThroughPipelineBehaviours) {
    command.incrementInvocationCount()
  }
}

class CommandThatFailsWhilePassingThroughPipelineBehaviours :
  EnrichedWithMetadata(),
  Command

class TestPipelineCommandHandlerThatFails : CommandHandler<CommandThatFailsWhilePassingThroughPipelineBehaviours> {
  override suspend fun handle(command: CommandThatFailsWhilePassingThroughPipelineBehaviours) {
    command.incrementInvocationCount()
    throw Exception()
  }
}

class TestCommandThatFailsWithException :
  EnrichedWithMetadata(),
  Command

class TestBrokenCommandHandler(
  private val mediator: MediatorAccessor
) : CommandHandler<TestCommandThatFailsWithException> {
  override suspend fun handle(command: TestCommandThatFailsWithException) {
    mediator shouldNotBe null
    command.incrementInvocationCount()
    throw Exception()
  }
}

class TestCommandForInheritance :
  EnrichedWithMetadata(),
  Command

abstract class MyCommandHandlerBaseForSpecificCommand : CommandHandler<TestCommandForInheritance>

class TestInheritedCommandHandlerForSpecificCommand : MyCommandHandlerBaseForSpecificCommand() {
  override suspend fun handle(command: TestCommandForInheritance) {
    command.incrementInvocationCount()
  }
}

class TestCommandForTypeLimitedInheritance :
  EnrichedWithMetadata(),
  Command

abstract class TestBaseCommandHandlerForTypeLimitedInheritance<TCommand : Command> : CommandHandler<TCommand>

class TestCommandHandlerForTypeLimitedInheritance :
  TestBaseCommandHandlerForTypeLimitedInheritance<TestCommandForTypeLimitedInheritance>() {
  override suspend fun handle(command: TestCommandForTypeLimitedInheritance) {
    command.incrementInvocationCount()
  }
}

class ParameterizedCommand<T>(
  val param: T
) : EnrichedWithMetadata(),
  Command

class ParameterizedCommandHandler<T> : CommandHandler<ParameterizedCommand<T>> {
  override suspend fun handle(command: ParameterizedCommand<T>) {
    command.param shouldNotBe null
    command.incrementInvocationCount()
  }
}

class ParameterizedCommandForInheritedCommandHandler<T>(
  val param: T
) : EnrichedWithMetadata(),
  Command

abstract class ParameterizedCommandHandlerBaseForInheritedCommandHandler<A> :
  CommandHandler<ParameterizedCommandForInheritedCommandHandler<A>>

class ParameterizedCommandHandlerForInheritance<A> : ParameterizedCommandHandlerBaseForInheritedCommandHandler<A>() {
  override suspend fun handle(command: ParameterizedCommandForInheritedCommandHandler<A>) {
    command.param shouldNotBe null
    command.incrementInvocationCount()
  }
}

class ParameterizedCommandWithResult<TParam, TReturn>(
  val param: TParam,
  val retFn: suspend (TParam) -> TReturn
) : EnrichedWithMetadata(),
  CommandWithResult<TReturn>

class ParameterizedCommandWithResultHandler<TParam, TReturn> :
  CommandWithResultHandler<ParameterizedCommandWithResult<TParam, TReturn>, TReturn> {
  override suspend fun handle(command: ParameterizedCommandWithResult<TParam, TReturn>): TReturn {
    command.param shouldNotBe null
    command.incrementInvocationCount()
    return command.retFn(command.param)
  }
}

data class ParameterizedCommandWithResultForInheritance<TParam>(
  val param: TParam
) : EnrichedWithMetadata(),
  CommandWithResult<String>

abstract class ParameterizedCommandWithResultHandlerBase<TParam : CommandWithResult<String>> : CommandWithResultHandler<TParam, String>

class ParameterizedCommandWithResultHandlerOfInheritedHandler<TParam> :
  ParameterizedCommandWithResultHandlerBase<ParameterizedCommandWithResultForInheritance<TParam>>() {
  override suspend fun handle(command: ParameterizedCommandWithResultForInheritance<TParam>): String {
    command.param shouldNotBe null
    command.incrementInvocationCount()
    return command.param.toString()
  }
}

/**
 * Queries
 */

class TestQuery(
  val id: Int
) : EnrichedWithMetadata(),
  Query<String>

class TestQueryHandler(
  private val mediator: MediatorAccessor
) : QueryHandler<TestQuery, String> {
  override suspend fun handle(query: TestQuery): String {
    mediator shouldNotBe null
    query.incrementInvocationCount()
    return "hello " + query.id
  }
}

class ParameterizedQuery<TParam, TResponse>(
  val param: TParam,
  val retFn: suspend (TParam) -> TResponse
) : EnrichedWithMetadata(),
  Query<TResponse>

class ParameterizedQueryHandler<TParam, TResponse> : QueryHandler<ParameterizedQuery<TParam, TResponse>, TResponse> {
  override suspend fun handle(query: ParameterizedQuery<TParam, TResponse>): TResponse {
    query.param shouldNotBe null
    query.incrementInvocationCount()
    return query.retFn(query.param)
  }
}

/**
 * Pipeline Behaviors
 */
interface CanPassExceptionPipelineBehaviour

class ExceptionPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
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
  override suspend fun <TRequest, TResponse> handle(
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
  override suspend fun <TRequest, TResponse> handle(
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
  Command,
  OrderedPipelineUseCase

class QueryThatPassesThroughOrderedPipelineBehaviours :
  EnrichedWithMetadata(),
  Query<String>,
  OrderedPipelineUseCase

class NotificationThatPassesThroughOrderedPipelineBehaviours :
  EnrichedWithMetadata(),
  Notification,
  OrderedPipelineUseCase

class CommandHandlerThatPassesThroughOrderedPipelineBehaviours : CommandHandler<CommandThatPassesThroughOrderedPipelineBehaviours> {
  override suspend fun handle(command: CommandThatPassesThroughOrderedPipelineBehaviours) {
    command.incrementInvocationCount()
  }
}

class QueryHandlerThatPassesThroughOrderedPipelineBehaviours : QueryHandler<QueryThatPassesThroughOrderedPipelineBehaviours, String> {
  override suspend fun handle(query: QueryThatPassesThroughOrderedPipelineBehaviours): String {
    query.incrementInvocationCount()
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

  override suspend fun <TRequest, TResponse> handle(
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

  override suspend fun <TRequest, TResponse> handle(
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

  override suspend fun <TRequest, TResponse> handle(
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
  Command {
  abstract val id: String

  data class TestCommandInherited1(
    override val id: String
  ) : TestCommandBase()

  data class TestCommandInherited2(
    override val id: String
  ) : TestCommandBase()
}

class TestCommandBaseHandler : CommandHandler<TestCommandBase> {
  override suspend fun handle(command: TestCommandBase) {
    command.incrementInvocationCount()
  }
}

sealed class TestQueryBase :
  EnrichedWithMetadata(),
  Query<String> {
  abstract val id: String

  data class TestQueryInherited1(
    override val id: String
  ) : TestQueryBase()

  data class TestQueryInherited2(
    override val id: String
  ) : TestQueryBase()
}

class TestQueryBaseHandler : QueryHandler<TestQueryBase, String> {
  override suspend fun handle(query: TestQueryBase): String {
    query.incrementInvocationCount()
    return query.id
  }
}

sealed class TestCommandWithResultBase :
  EnrichedWithMetadata(),
  CommandWithResult<String> {
  abstract val id: String

  data class TestCommandWithResultInherited1(
    override val id: String
  ) : TestCommandWithResultBase()

  data class TestCommandWithResultInherited2(
    override val id: String
  ) : TestCommandWithResultBase()
}

class TestCommandWithResultBaseHandler : CommandWithResultHandler<TestCommandWithResultBase, String> {
  override suspend fun handle(command: TestCommandWithResultBase): String {
    command.incrementInvocationCount()
    return "${command.id} handled"
  }
}
