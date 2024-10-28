package com.trendyol.kediatr.framewokUseCases

import com.trendyol.kediatr.*
import io.kotest.matchers.shouldNotBe

class TestNonExistCommand : Command

class TestCommand : Command

class TestCommandHandler(
  private val mediator: Mediator
) : CommandHandler<TestCommand> {
  override suspend fun handle(command: TestCommand) {
    mediator shouldNotBe null
  }
}

class Result(
  val value: Int = 0
)

class NonExistCommandWithResult : CommandWithResult<Result>

data class TestCommandWithResult(val invoked: Int = 0) : CommandWithResult<Result>

class TestCommandWithResultCommandHandler(
  val mediator: Mediator
) : CommandWithResultHandler<TestCommandWithResult, Result> {
  override suspend fun handle(command: TestCommandWithResult): Result = Result(command.invoked + 1)
}

class TestNotification : Notification

class TestNotificationHandler(
  private val mediator: Mediator
) : NotificationHandler<TestNotification> {
  override suspend fun handle(notification: TestNotification) {
    mediator shouldNotBe null
  }
}

class TestBrokenCommand : Command

class TestPipelineCommand : Command

class TestPipelineCommandHandler(
  val mediator: Mediator
) : CommandHandler<TestPipelineCommand> {
  override suspend fun handle(command: TestPipelineCommand) {
    mediator shouldNotBe null
  }
}

class TestBrokenCommandHandler(
  private val mediator: Mediator
) : CommandHandler<TestBrokenCommand> {
  override suspend fun handle(command: TestBrokenCommand) {
    mediator shouldNotBe null
    throw Exception()
  }
}

class ExceptionPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    try {
//      exceptionPipelineBehaviorHandleCounter++
      return next(request)
    } catch (ex: Exception) {
//      exceptionPipelineBehaviorHandleCatchCounter++
      throw ex
    }
  }
}

class LoggingPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
//    loggingPipelineBehaviorHandleBeforeNextCounter++
    val result = next(request)
//    loggingPipelineBehaviorHandleAfterNextCounter++
    return result
  }
}

class NonExistQuery : Query<String>

class TestQuery(val id: Int) : Query<String>

class TestQueryHandler(
  private val mediator: Mediator
) : QueryHandler<TestQuery, String> {
  override suspend fun handle(query: TestQuery): String {
    mediator shouldNotBe null
    return "hello " + query.id
  }
}
