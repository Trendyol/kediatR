# kediatR Migration Guide

## From v3.x to v4.x Breaking Changes: Complete Architecture Overhaul

This migration guide covers the major breaking changes introduced in v4.x, which includes a complete architecture overhaul with unified request/response patterns and simplified API design.

### Summary of Changes

#### Removed Interfaces and Classes
- **Removed**: `CommandWithResult<TResult>` interface
- **Removed**: `CommandWithResultHandler<TCommand, TResult>` interface
- **Removed**: `Command` interface (replaced with `Request<TResult>`)
- **Removed**: `CommandHandler<TCommand>` interface (replaced with `RequestHandler<TRequest, TResult>`)
- **Removed**: `Query<TResult>` interface (replaced with `Request<TResult>`)
- **Removed**: `QueryHandler<TQuery, TResult>` interface (replaced with `RequestHandler<TRequest, TResult>`)
- **Removed**: `MediatorBuilder` class
- **Removed**: `CommandProvider`, `QueryProvider` classes (replaced with `RequestProvider`)

#### New Unified Architecture
- **Added**: `Message` sealed interface as the base for all messages
- **Added**: `Request<TResponse>` interface for all request types (commands and queries)
- **Added**: `Request.Unit` nested interface for requests that don't return results
- **Added**: `RequestHandler<TRequest, TResult>` interface for all request handlers
- **Added**: `RequestHandler.Unit<TRequest>` nested interface for unit request handlers
- **Modified**: `Mediator` interface now has a unified `send()` method for all requests
- **Modified**: `Mediator.publish()` now requires explicit `PublishStrategy` parameter (with default)
- **Modified**: `PipelineBehavior` now works with `Message` instead of separate types
- **Modified**: All dependency providers now use `RequestHandler` instead of separate command/query handlers

### Before (Old API)

```kotlin
// Commands (no result)
interface Command {
  val type: Class<out Command> get() = this::class.java
}

interface CommandHandler<TCommand : Command> {
  suspend fun handle(command: TCommand)
}

// Commands with results
interface CommandWithResult<TResult> {
  val type: Class<out CommandWithResult<TResult>> get() = this::class.java
}

interface CommandWithResultHandler<TCommand : CommandWithResult<TResult>, TResult> {
  suspend fun handle(command: TCommand): TResult
}

// Queries
interface Query<TResult> {
  val type: Class<out Query<TResult>> get() = this::class.java
}

interface QueryHandler<TQuery : Query<TResult>, TResult> {
  suspend fun handle(query: TQuery): TResult
}

// Mediator with separate methods
interface Mediator {
  suspend fun <TQuery : Query<TResponse>, TResponse> send(query: TQuery): TResponse
  suspend fun <TCommand : Command<TResult>, TResult> send(command: TCommand): TResult
  suspend fun <T : Notification> publish(notification: T)
  suspend fun <T : Notification> publish(notification: T, publishStrategy: PublishStrategy)
}

// MediatorBuilder for configuration
class MediatorBuilder(dependencyProvider: DependencyProvider) {
  fun withPublishStrategy(strategy: PublishStrategy): MediatorBuilder
  fun build(): Mediator
}
```

### After (New API)

```kotlin
// Unified message hierarchy
sealed interface Message

// Unified request interface for commands and queries
interface Request<out TResponse> : Message {
  interface Unit : Request<kotlin.Unit>
}

// Unified request handler interface
interface RequestHandler<TRequest : Request<TResult>, TResult> {
  suspend fun handle(request: TRequest): TResult
  
  interface Unit<TRequest : Request.Unit> : RequestHandler<TRequest, kotlin.Unit>
}

// Notifications remain the same but now extend Message
interface Notification : Message

// Simplified Mediator interface
interface Mediator {
  suspend fun <TRequest : Request<TResponse>, TResponse> send(request: TRequest): TResponse
  suspend fun <T : Notification> publish(notification: T, publishStrategy: PublishStrategy = PublishStrategy.DEFAULT)
  
  companion object {
    fun build(dependencyProvider: DependencyProvider): Mediator
  }
}
```

## Migration Steps

### 0. Optional: Use Type Aliases for Gradual Migration

To ease the migration process, you can temporarily add type aliases to your codebase. This allows you to migrate incrementally without breaking existing code:

```kotlin
// Add these type aliases to ease migration
typealias Command = Request.Unit
typealias CommandWithResult<T> = Request<T>
typealias CommandHandler<TCommand> = RequestHandler.Unit<TCommand>
typealias CommandWithResultHandler<TCommand, TResult> = RequestHandler<TCommand, TResult>
typealias Query<T> = Request<T>
typealias QueryHandler<TQuery, TResult> = RequestHandler<TQuery, TResult>
```

**Benefits:**
- Allows gradual migration without breaking existing code
- Helps during large codebase migrations
- Can be removed once migration is complete

**Usage:**
1. Add the type aliases to a common file (e.g., `TypeAliases.kt`)
2. Import them where needed
3. Gradually replace usage with the new interfaces
4. Remove the type aliases once migration is complete

### 1. Update Unit Commands

**Before:**
```kotlin
class CreateUserCommand : Command {
  // command properties
}

class CreateUserCommandHandler : CommandHandler<CreateUserCommand> {
  override suspend fun handle(command: CreateUserCommand) {
    // handle command
  }
}
```

**After:**
```kotlin
class CreateUserCommand : Request.Unit {
  // command properties
}

class CreateUserCommandHandler : RequestHandler.Unit<CreateUserCommand> {
  override suspend fun handle(request: CreateUserCommand) {
    // handle command
  }
}
```

### 2. Update Commands with Results

**Before:**
```kotlin
class GetUserCommand(val userId: String) : CommandWithResult<User> {
  // command properties
}

class GetUserCommandHandler : CommandWithResultHandler<GetUserCommand, User> {
  override suspend fun handle(command: GetUserCommand): User {
    // handle command and return result
    return userRepository.findById(command.userId)
  }
}
```

**After:**
```kotlin
class GetUserCommand(val userId: String) : Request<User> {
  // command properties
}

class GetUserCommandHandler : RequestHandler<GetUserCommand, User> {
  override suspend fun handle(request: GetUserCommand): User {
    // handle command and return result
    return userRepository.findById(request.userId)
  }
}
```

### 3. Update Queries

**Before:**
```kotlin
class GetUserQuery(val userId: String) : Query<User> {
  // query properties
}

class GetUserQueryHandler : QueryHandler<GetUserQuery, User> {
  override suspend fun handle(query: GetUserQuery): User {
    // handle query and return result
    return userRepository.findById(query.userId)
  }
}
```

**After:**
```kotlin
class GetUserQuery(val userId: String) : Request<User> {
  // query properties
}

class GetUserQueryHandler : RequestHandler<GetUserQuery, User> {
  override suspend fun handle(request: GetUserQuery): User {
    // handle query and return result
    return userRepository.findById(request.userId)
  }
}
```

### 4. Update Mediator Usage

The mediator usage remains mostly the same, but now all requests use the unified `send()` method:

```kotlin
// Unit commands (no change)
mediator.send(CreateUserCommand())

// Commands with results (no change)
val user = mediator.send(GetUserCommand("123"))

// Queries (no change)
val user = mediator.send(GetUserQuery("123"))

// Notifications now require explicit PublishStrategy (with default)
mediator.publish(UserCreatedNotification(user.id)) // Uses default strategy
mediator.publish(UserCreatedNotification(user.id), PublishStrategy.PARALLEL_NO_WAIT)
```

### 5. Update Mediator Creation

**Before:**
```kotlin
val mediator = MediatorBuilder(dependencyProvider)
  .withPublishStrategy(ParallelNoWaitPublishStrategy())
  .build()
```

**After:**
```kotlin
val mediator = Mediator.build(dependencyProvider)
// PublishStrategy is now specified per publish call
```

### 6. Update Dependency Injection Registration

Handler registration needs to be updated to use the new interfaces:

#### Spring Boot
```kotlin
@Component
class CreateUserCommandHandler : RequestHandler.Unit<CreateUserCommand> {
  // implementation
}

@Component  
class GetUserCommandHandler : RequestHandler<GetUserCommand, User> {
  // implementation
}

@Component
class GetUserQueryHandler : RequestHandler<GetUserQuery, User> {
  // implementation
}
```

#### Koin
```kotlin
module {
  single { CreateUserCommandHandler() } bind RequestHandler::class
  single { GetUserCommandHandler() } bind RequestHandler::class
  single { GetUserQueryHandler() } bind RequestHandler::class
}
```

#### Manual Registration
```kotlin
val mediator = HandlerRegistryProvider.createMediator(
  handlers = listOf(
    CreateUserCommandHandler(),
    GetUserCommandHandler(),
    GetUserQueryHandler()
  )
)
```

## Advanced Migration Scenarios

### 1. Parameterized Commands

**Before:**
```kotlin
class ParameterizedCommand<T>(val param: T) : Command

class ParameterizedCommandHandler<T> : CommandHandler<ParameterizedCommand<T>> {
  override suspend fun handle(command: ParameterizedCommand<T>) {
    // handle
  }
}
```

**After:**
```kotlin
class ParameterizedCommand<T>(val param: T) : Request.Unit

class ParameterizedCommandHandler<T> : RequestHandler.Unit<ParameterizedCommand<T>> {
  override suspend fun handle(request: ParameterizedCommand<T>) {
    // handle
  }
}
```

### 2. Parameterized Commands with Results

**Before:**
```kotlin
class ParameterizedCommandWithResult<TParam, TReturn>(
  val param: TParam,
  val retFn: suspend (TParam) -> TReturn
) : CommandWithResult<TReturn>

class ParameterizedCommandWithResultHandler<TParam, TReturn> : 
  CommandWithResultHandler<ParameterizedCommandWithResult<TParam, TReturn>, TReturn> {
  override suspend fun handle(command: ParameterizedCommandWithResult<TParam, TReturn>): TReturn {
    return command.retFn(command.param)
  }
}
```

**After:**
```kotlin
class ParameterizedCommandWithResult<TParam, TReturn>(
  val param: TParam,
  val retFn: suspend (TParam) -> TReturn
) : Request<TReturn>

class ParameterizedCommandWithResultHandler<TParam, TReturn> : 
  RequestHandler<ParameterizedCommandWithResult<TParam, TReturn>, TReturn> {
  override suspend fun handle(request: ParameterizedCommandWithResult<TParam, TReturn>): TReturn {
    return request.retFn(request.param)
  }
}
```

### 3. Inheritance Scenarios

**Before:**
```kotlin
sealed class BaseCommand : Command {
  abstract val id: String
}

class SpecificCommand(override val id: String) : BaseCommand()

class BaseCommandHandler : CommandHandler<BaseCommand> {
  override suspend fun handle(command: BaseCommand) {
    // handle
  }
}
```

**After:**
```kotlin
sealed class BaseCommand : Request.Unit {
  abstract val id: String
}

class SpecificCommand(override val id: String) : BaseCommand()

class BaseCommandHandler : RequestHandler.Unit<BaseCommand> {
  override suspend fun handle(request: BaseCommand) {
    // handle
  }
}
```

### 4. Pipeline Behaviors

**Before:**
```kotlin
class LoggingPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    println("Before: $request")
    val response = next(request)
    println("After: $response")
    return response
  }
}
```

**After:**
```kotlin
class LoggingPipelineBehavior : PipelineBehavior {
  override suspend fun <TRequest : Message, TResponse> handle(
    request: TRequest,
    next: RequestHandlerDelegate<TRequest, TResponse>
  ): TResponse {
    println("Before: $request")
    val response = next(request)
    println("After: $response")
    return response
  }
}
```

## Benefits of the New API

1. **Unified Architecture**: Single `Request<TResult>` interface for all request types (commands and queries)
2. **Simplified API**: Fewer interfaces to understand - only `Request`, `RequestHandler`, and `Notification`
3. **Type Safety**: Better compile-time type checking with generic result types
4. **Consistent Patterns**: All requests follow the same pattern regardless of type
5. **Cleaner Dependency Injection**: Single `RequestHandler` interface for all DI frameworks
6. **Flexible Publishing**: Explicit control over notification publishing strategies
7. **Better Testability**: Simpler mocking with unified handler interface
8. **Future-Proof**: Extensible architecture that can accommodate new request types

## Checklist for Migration

- [ ] Replace `Command` implementations with `Request.Unit`
- [ ] Replace `CommandHandler<TCommand>` with `RequestHandler.Unit<TCommand>`
- [ ] Replace `CommandWithResult<TResult>` with `Request<TResult>`
- [ ] Replace `CommandWithResultHandler<TCommand, TResult>` with `RequestHandler<TCommand, TResult>`
- [ ] Replace `Query<TResult>` implementations with `Request<TResult>`
- [ ] Replace `QueryHandler<TQuery, TResult>` with `RequestHandler<TQuery, TResult>`
- [ ] Update `MediatorBuilder` usage to use `Mediator.build()` directly
- [ ] Update `Mediator.publish()` calls to include explicit `PublishStrategy` (optional, has default)
- [ ] Update pipeline behaviors to use `<TRequest : Message, TResponse>` constraint
- [ ] Update dependency injection registrations to use `RequestHandler` instead of separate handler types
- [ ] Update import statements to remove references to deleted interfaces
- [ ] Update handler method parameters from `command`/`query` to `request`
- [ ] Test all handlers to ensure they work correctly
- [ ] Update any custom extensions or utilities that referenced the old interfaces

## Troubleshooting

### Common Compilation Errors

1. **"Unresolved reference: Command"**
   - Replace with `Request.Unit` for unit commands

2. **"Unresolved reference: CommandWithResult"**
   - Replace with `Request<TResult>`

3. **"Unresolved reference: CommandWithResultHandler"**
   - Replace with `RequestHandler<TCommand, TResult>`

4. **"Unresolved reference: Query"**
   - Replace with `Request<TResult>`

5. **"Unresolved reference: QueryHandler"**
   - Replace with `RequestHandler<TQuery, TResult>`

6. **"Unresolved reference: CommandHandler"**
   - Replace with `RequestHandler.Unit<TCommand>` for unit commands
   - Replace with `RequestHandler<TCommand, TResult>` for commands with results

7. **"Unresolved reference: MediatorBuilder"**
   - Replace with `Mediator.build(dependencyProvider)`

8. **"Type mismatch" errors on unit commands**
   - Ensure unit commands implement `Request.Unit`
   - Ensure unit handlers implement `RequestHandler.Unit<TCommand>`

9. **"Wrong number of type arguments" on pipeline behaviors**
   - Add `Message` constraint: `<TRequest : Message, TResponse>`

### Runtime Issues

1. **HandlerNotFoundException**
   - Ensure handlers are properly registered with dependency injection
   - Check that command and handler types match exactly

2. **ClassCastException**
   - Verify generic type parameters are correctly specified
   - Ensure handler return types match command result types

## Support

If you encounter issues during migration, please:

1. Check this migration guide thoroughly
2. Review the test examples in the `testFixtures` directory
3. Create an issue in the GitHub repository with:
   - Your current code
   - The error message
   - Expected behavior
