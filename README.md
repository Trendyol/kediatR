# kediatR [![codecov](https://codecov.io/gh/trendyol/kediatr/branch/main/graph/badge.svg)](https://codecov.io/gh/trendyol/kediatr)

<img style="float: left" alt="Humus! The kediatr mascot" src="/humus.png" alt="drawing" width="80"/>

Mediator implementation in kotlin with native coroutine support. Supports Spring-Boot, Quarkus and Koin dependency
providers.

Documentation is available at [https://trendyol.github.io/kediatR/](https://trendyol.github.io/kediatR/)

## Show me the code

```kotlin
class PingCommand : Command // or
class PingQuery : Query<String> // or
class PingNotification : Notification
class PingCommandHandler : CommandHandler<PingCommand> {
  override suspend fun handle(command: PingCommand) {
    println("Pong!")
  }
}
class PingQueryHandler : QueryHandler<PingQuery, String> {
  override suspend fun handle(query: PingQuery): String {
    return "Pong!"
  }
}

class PingNotificationHandler : NotificationHandler<PingNotification> {
  override suspend fun handle(notification: PingNotification) {
    println("Pong!")
  }
}

class MeasurePipelineBehaviour : PipelineBehaviour {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    val start = System.currentTimeMillis()
    val response = next(request)
    val end = System.currentTimeMillis()
    println("Request ${request::class.simpleName} took ${end - start} ms")
    return response
  }
}

val mediator = // create mediator instance in-memory or with dependency injection, take a look at the documentation
  mediator.send(PingCommand()) // 1..1
mediator.send(PingQuery()) // 1..1
mediator.send(PingNotification()) // 0..N
```
