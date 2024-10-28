@file:Suppress("UNCHECKED_CAST")

package com.trendyol.kediatr.framewokUseCases

import com.trendyol.kediatr.*
import io.kotest.matchers.shouldNotBe

class TestNonExistCommand : Command

class TestCommand : Command, EnrichedWithMetadata()

class TestCommandHandler(
  private val mediator: Mediator
) : CommandHandler<TestCommand> {
  override suspend fun handle(command: TestCommand) {
    mediator shouldNotBe null
    command.incrementInvocationCount()
  }
}

class Result(
  val value: Int = 0
)

class NonExistCommandWithResult : CommandWithResult<Result>

data class TestCommandWithResult(
  val invoked: Int = 0
) : CommandWithResult<Result>, EnrichedWithMetadata()

class TestCommandWithResultCommandHandler(
  val mediator: Mediator
) : CommandWithResultHandler<TestCommandWithResult, Result> {
  override suspend fun handle(
    command: TestCommandWithResult
  ): Result = Result(command.invoked + 1).also { command.incrementInvocationCount() }
}

class TestNotification : Notification, EnrichedWithMetadata()

class TestNotificationHandler(
  private val mediator: Mediator
) : NotificationHandler<TestNotification> {
  override suspend fun handle(notification: TestNotification) {
    mediator shouldNotBe null
    notification.incrementInvocationCount()
  }
}

class TestBrokenCommand : Command, EnrichedWithMetadata()

class TestPipelineCommand : Command, EnrichedWithMetadata()

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

class TestPipelineCommandHandler(
  val mediator: Mediator
) : CommandHandler<TestPipelineCommand> {
  override suspend fun handle(command: TestPipelineCommand) {
    mediator shouldNotBe null
    command.incrementInvocationCount()
  }
}

class TestBrokenCommandHandler(
  private val mediator: Mediator
) : CommandHandler<TestBrokenCommand> {
  override suspend fun handle(command: TestBrokenCommand) {
    mediator shouldNotBe null
    command.incrementInvocationCount()
    throw Exception()
  }
}

class ExceptionPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = try {
    when (request) {
      is EnrichedWithMetadata -> request.visitedPipeline(this::class.java.simpleName)
    }
    next(request)
  } catch (ex: Exception) {
    throw ex
  }
}

class LoggingPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    when (request) {
      is EnrichedWithMetadata -> request.visitedPipeline(this::class.java.simpleName)
    }
    return next(request)
  }
}

class NonExistQuery : Query<String>

class TestQuery(val id: Int) : Query<String>, EnrichedWithMetadata()

class TestQueryHandler(
  private val mediator: Mediator
) : QueryHandler<TestQuery, String> {
  override suspend fun handle(query: TestQuery): String {
    mediator shouldNotBe null
    query.incrementInvocationCount()
    return "hello " + query.id
  }
}

class TestCommandForInheritance : Command, EnrichedWithMetadata()

abstract class MyCommandHandlerBaseForSpecificCommand : CommandHandler<TestCommandForInheritance>

class TestInheritedCommandHandlerForSpecificCommand : MyCommandHandlerBaseForSpecificCommand() {
  override suspend fun handle(command: TestCommandForInheritance) {
    command.incrementInvocationCount()
  }
}
