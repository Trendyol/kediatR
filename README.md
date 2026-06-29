<div align="center">

<img alt="Humus! The kediatr mascot" src="/humus.png" width="80"/>

# kediatR

![Release](https://img.shields.io/maven-central/v/com.trendyol/kediatr-core?label=latest-release&color=blue) [![codecov](https://codecov.io/gh/trendyol/kediatr/branch/main/graph/badge.svg)](https://codecov.io/gh/trendyol/kediatr) [![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/Trendyol/kediatR/badge)](https://scorecard.dev/viewer/?uri=github.com/Trendyol/kediatR)

</div>

Mediator implementation in Kotlin with native coroutine support. Send requests to a single handler, publish
notifications to many, and wrap both in pipeline behaviors — all `suspend`-first. Ships dependency-provider
integrations for Spring Boot, Quarkus and Koin.

> [!TIP]
> "kedi" in Turkish means cat 🐱 and kediatR rhymes with the C# equivalent library [mediatR](https://github.com/jbogard/MediatR) :)

## Contents

- [Install](#install)
- [Concepts](#concepts)
- [Quick start](#quick-start)
- [Requests](#requests)
- [Notifications](#notifications)
- [Publish strategies](#publish-strategies)
- [Pipeline behaviors](#pipeline-behaviors)
- [Handler inheritance](#handler-inheritance)
- [Dependency injection](#dependency-injection)
  - [Spring Boot](#spring-boot)
  - [Koin](#koin)
  - [Quarkus](#quarkus)
- [Migrating from 3.x](#migrating-from-3x)
- [IntelliJ plugin](#intellij-plugin)

## Install

Pick the version from [releases](https://github.com/Trendyol/kediatR/releases), then add the modules you need.

```kotlin
val version = "{CURRENT_VERSION}"

// Core — always required
implementation("com.trendyol:kediatr-core:$version")

// One starter, matching your framework (each pulls in core transitively)
implementation("com.trendyol:kediatr-spring-boot-2x-starter:$version")
implementation("com.trendyol:kediatr-spring-boot-3x-starter:$version")
implementation("com.trendyol:kediatr-spring-boot-4x-starter:$version")
implementation("com.trendyol:kediatr-koin-starter:$version")
implementation("com.trendyol:kediatr-quarkus-starter:$version")
```

Core alone is enough for plain Kotlin apps — the starters only add the wiring to resolve handlers from a DI
container.

## Concepts

Everything flows through the `Mediator`. There are exactly three message kinds:

| Message        | Handlers     | Returns | Dispatched with      |
|----------------|--------------|---------|----------------------|
| `Request<T>`   | exactly one  | `T`     | `mediator.send()`    |
| `Request.Unit` | exactly one  | nothing | `mediator.send()`    |
| `Notification` | zero or more | nothing | `mediator.publish()` |

A `Request` unifies what other libraries split into "commands" and "queries" — both are just a request that
returns a value (use `Request.Unit` when there's nothing to return). Each request has **one** handler.
A `Notification` is fire-and-forget and can fan out to **many** handlers.

`PipelineBehavior` wraps every `send` and `publish`, giving you one place for cross-cutting concerns
(logging, validation, transactions, metrics).

All handlers are `suspend` functions — kediatR is coroutine-native, no thread-blocking bridges.

## Quick start

Without a DI framework, build a mediator from a list of handler instances:

```kotlin
import com.trendyol.kediatr.*

class Ping : Request<String>

class PingHandler : RequestHandler<Ping, String> {
  override suspend fun handle(request: Ping): String = "Pong!"
}

suspend fun main() {
  val mediator = HandlerRegistryProvider.createMediator(
    handlers = listOf(PingHandler())
  )

  println(mediator.send(Ping())) // Pong!
}
```

`createMediator` accepts any mix of `RequestHandler`, `NotificationHandler` and `PipelineBehavior` instances.
For DI-managed apps, see [Dependency injection](#dependency-injection).

## Requests

A request returns a value; declare the return type as the type parameter.

```kotlin
data class GetUserById(val id: Long) : Request<User>

class GetUserByIdHandler(
  private val users: UserRepository
) : RequestHandler<GetUserById, User> {
  override suspend fun handle(request: GetUserById): User =
    users.findById(request.id)
}

val user: User = mediator.send(GetUserById(42))
```

When a request has no meaningful return value, use `Request.Unit` and `RequestHandler.Unit`:

```kotlin
data class DeleteUser(val id: Long) : Request.Unit

class DeleteUserHandler(
  private val users: UserRepository
) : RequestHandler.Unit<DeleteUser> {
  override suspend fun handle(request: DeleteUser) {
    users.delete(request.id)
  }
}

mediator.send(DeleteUser(42))
```

Sending a request with no registered handler throws `HandlerNotFoundException`. Any exception thrown inside a
handler propagates to the caller unchanged.

## Notifications

A notification is delivered to every registered handler. Publishing one with no handlers is a no-op.

```kotlin
data class UserCreated(val userId: Long) : Notification

class SendWelcomeEmail(private val email: EmailService) : NotificationHandler<UserCreated> {
  override suspend fun handle(notification: UserCreated) {
    email.sendWelcome(notification.userId)
  }
}

class WarmCache(private val cache: Cache) : NotificationHandler<UserCreated> {
  override suspend fun handle(notification: UserCreated) {
    cache.invalidateUsers()
  }
}

mediator.publish(UserCreated(42)) // both handlers run
```

## Publish strategies

`publish` takes an optional `PublishStrategy` controlling how multiple handlers run and how failures behave.
The default is `PublishStrategy.DEFAULT`.

| Strategy                | Execution             | On failure                                                       |
|-------------------------|-----------------------|-----------------------------------------------------------------|
| `DEFAULT`               | sequential            | stops at the first failing handler, rethrows that exception      |
| `CONTINUE_ON_EXCEPTION` | sequential            | runs all handlers, then throws `AggregateException` if any failed |
| `PARALLEL_WHEN_ALL`     | parallel (`awaitAll`) | runs all handlers, propagates a failure after all complete       |

```kotlin
mediator.publish(UserCreated(42)) // DEFAULT

mediator.publish(UserCreated(42), PublishStrategy.PARALLEL_WHEN_ALL)

try {
  mediator.publish(UserCreated(42), PublishStrategy.CONTINUE_ON_EXCEPTION)
} catch (e: AggregateException) {
  e.exceptions.forEach { log.error("handler failed", it) }
}
```

Handlers run on `Dispatchers.IO` by default. You can implement `PublishStrategy` yourself for custom behavior.

## Pipeline behaviors

A `PipelineBehavior` wraps the handling of **every** request and notification, so you can run logic before and
after the inner handler. Implement `handle`, do your work, and call `next` to continue the chain.

```kotlin
class LoggingBehavior : PipelineBehavior {
  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    log.info("handling ${request::class.simpleName}")
    val response = next(request)
    log.info("handled ${request::class.simpleName}")
    return response
  }
}
```

Register a behavior the same way as a handler (in the `createMediator` list, or as a DI bean) and it applies
automatically.

### Ordering

When several behaviors are present they run sorted by `order` — **lowest value runs first** (outermost). Override
`order` to control the chain; it defaults to `PipelineBehavior.HIGHEST_PRECEDENCE` (`Int.MIN_VALUE`).

```kotlin
class FirstBehavior : PipelineBehavior {
  override val order: Int = 1
  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = next(request)
}

class SecondBehavior : PipelineBehavior {
  override val order: Int = 2 // runs after FirstBehavior
  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse = next(request)
}
```

`PipelineBehavior.HIGHEST_PRECEDENCE` and `LOWEST_PRECEDENCE` are provided as convenience constants.

## Handler inheritance

Handlers resolve polymorphically. A handler registered for a base type also handles its subtypes, and a
notification handler for a base notification receives all derived notifications. This works for both requests and
notifications, including generic/parameterized types.

```kotlin
sealed class DomainEvent : Notification
data class OrderPlaced(val orderId: Long) : DomainEvent()
data class OrderShipped(val orderId: Long) : DomainEvent()

// Receives every DomainEvent subtype
class AuditLogger : NotificationHandler<DomainEvent> {
  override suspend fun handle(notification: DomainEvent) {
    audit.record(notification)
  }
}
```

For requests, a more specific handler takes priority over a base-type handler when both are registered.

## Dependency injection

Each starter adapts a DI container to kediatR's `DependencyProvider`. Register your handlers/behaviors as beans;
the starter discovers them and wires up a ready-to-inject `Mediator`. Nothing about the handler code changes
between frameworks — only how you register them.

### Spring Boot

Add the starter matching your Spring Boot major version (`2x`, `3x`, or `4x`). Auto-configuration exposes a
`Mediator` bean; annotate handlers and behaviors as Spring components.

```kotlin
@Component
class GetUserByIdHandler(
  private val users: UserRepository
) : RequestHandler<GetUserById, User> {
  override suspend fun handle(request: GetUserById): User =
    users.findById(request.id)
}

@Service
class UserService(private val mediator: Mediator) {
  suspend fun find(id: Long): User = mediator.send(GetUserById(id))
}
```

The `Mediator` bean is `@ConditionalOnMissingBean` — define your own to override it.

### Koin

Provide the mediator with `KediatRKoin.getMediator()` and register handlers as regular Koin definitions. The
mediator must be created in a module that can see your handlers (it resolves them from the running Koin
instance).

```kotlin
val appModule = module {
  single { KediatRKoin.getMediator() }

  // Handlers & behaviors as plain singles
  single { GetUserByIdHandler(get()) }
  single { LoggingBehavior() }
}

class UserService(private val mediator: Mediator) {
  suspend fun find(id: Long): User = mediator.send(GetUserById(id))
}
```

### Quarkus

Add the starter and register handlers as CDI beans (`@ApplicationScoped`). A `Mediator` is produced for you.

Quarkus does not index third-party libraries unless told to. Add this to `application.properties`:

```properties
quarkus.index-dependency.kediatr.group-id=com.trendyol
quarkus.index-dependency.kediatr.artifact-id=kediatr-quarkus-starter
```

```kotlin
@ApplicationScoped
class GetUserByIdHandler(
  private val users: UserRepository
) : RequestHandler<GetUserById, User> {
  override suspend fun handle(request: GetUserById): User =
    users.findById(request.id)
}

@ApplicationScoped
class UserService(private val mediator: Mediator) {
  suspend fun find(id: Long): User = mediator.send(GetUserById(id))
}
```

## Migrating from 3.x

4.x is a breaking redesign: `Command`/`Query` and their handlers are unified into `Request`/`RequestHandler`,
and `MediatorBuilder` is replaced by `Mediator.build()`. See the full
[migration guide](MIGRATION_GUIDE.md) for a step-by-step walkthrough and type-alias shims for incremental
migration.

## IntelliJ plugin

A community IntelliJ plugin helps navigate between messages and their handlers:
<https://plugins.jetbrains.com/plugin/16017-kediatr-helper> (source:
<https://github.com/bilal-kilic/kediatr-helper>).
