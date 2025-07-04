# kediatR Migration Guide

## From v3.x to v4.x Breaking Changes: Command Type Hierarchy Unification

This migration guide covers the breaking changes introduced in the command type hierarchy unification. The main change is the removal of `CommandWithResult` and `CommandWithResultHandler` interfaces in favor of a unified `Command<TResult>` interface.

### Summary of Changes

- **Removed**: `CommandWithResult` interface
- **Removed**: `CommandWithResultHandler` interface
- **Removed** `MediatorBuilder` class hence the usage of PublishStrategy is now directly from `Mediator#publish(notification, strategy = default)`
- **Removed** `Mediator#publish(notification)` since have already interface method with `PublishStrategy` -> `Mediator#publish(notification, strategy = default)` this is not a breaking change.
- **Modified**: `Command` interface now accepts a generic type parameter `TResult`
- **Added**: `Command.Unit` nested interface for commands that don't return results
- **Modified**: `CommandHandler` interface now handles both unit and result-returning commands
- **Added**: `CommandHandler.Unit` nested interface for unit command handlers

### Before (Old API)

```kotlin
// Unit commands (no result)
interface Command {
}

interface CommandHandler<TCommand : Command> {
  suspend fun handle(command: TCommand)
}

// Commands with results
interface CommandWithResult<TResult> {
}

interface CommandWithResultHandler<TCommand : CommandWithResult<TResult>, TResult> {
  suspend fun handle(command: TCommand): TResult
}
```

### After (New API)

```kotlin
// Unified command interface
interface Command<TResult> {
  // Nested interface for unit commands
  interface Unit : Command<kotlin.Unit>
}

// Unified command handler interface
interface CommandHandler<TCommand : Command<TResult>, TResult> {
  suspend fun handle(command: TCommand): TResult

  // Nested interface for unit command handlers
  interface Unit<TCommand : Command.Unit> : CommandHandler<TCommand, kotlin.Unit>
}
```

## Migration Steps

### 0. Optional: Use Type Aliases for Gradual Migration

To ease the migration process, you can temporarily add type aliases to your codebase. This allows you to migrate incrementally without breaking existing code:

```kotlin
// Add these type aliases to ease migration
typealias CommandWithResult<T> = Command<T>
typealias CommandWithResultHandler<TCommand, TResult> = CommandHandler<TCommand, TResult>
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
class CreateUserCommand : Command.Unit {
  // command properties
}

class CreateUserCommandHandler : CommandHandler.Unit<CreateUserCommand> {
  override suspend fun handle(command: CreateUserCommand) {
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
class GetUserCommand(val userId: String) : Command<User> {
  // command properties
}

class GetUserCommandHandler : CommandHandler<GetUserCommand, User> {
  override suspend fun handle(command: GetUserCommand): User {
    // handle command and return result
    return userRepository.findById(command.userId)
  }
}
```

### 3. Update Mediator Usage

The mediator usage remains the same - no changes needed:

```kotlin
// Unit commands
mediator.send(CreateUserCommand())

// Commands with results
val user = mediator.send(GetUserCommand("123"))
```

### 4. Update Dependency Injection Registration

The handler registration remains the same since the framework automatically detects the correct handler types:

#### Spring Boot
```kotlin
@Component
class CreateUserCommandHandler : CommandHandler.Unit<CreateUserCommand> {
  // implementation
}

@Component  
class GetUserCommandHandler : CommandHandler<GetUserCommand, User> {
  // implementation
}
```

#### Koin
```kotlin
module {
  single { CreateUserCommandHandler() } bind CommandHandler::class
  single { GetUserCommandHandler() } bind CommandHandler::class
}
```

#### Manual Registration
```kotlin
val mediator = MappingDependencyProvider.createMediator(
  handlers = listOf(
    CreateUserCommandHandler(),
    GetUserCommandHandler()
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
class ParameterizedCommand<T>(val param: T) : Command.Unit

class ParameterizedCommandHandler<T> : CommandHandler.Unit<ParameterizedCommand<T>> {
  override suspend fun handle(command: ParameterizedCommand<T>) {
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
) : Command<TReturn>

class ParameterizedCommandWithResultHandler<TParam, TReturn> : 
  CommandHandler<ParameterizedCommandWithResult<TParam, TReturn>, TReturn> {
  override suspend fun handle(command: ParameterizedCommandWithResult<TParam, TReturn>): TReturn {
    return command.retFn(command.param)
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
sealed class BaseCommand : Command.Unit {
  abstract val id: String
}

class SpecificCommand(override val id: String) : BaseCommand()

class BaseCommandHandler : CommandHandler.Unit<BaseCommand> {
  override suspend fun handle(command: BaseCommand) {
    // handle
  }
}
```

## Benefits of the New API

1. **Unified Interface**: Single `Command<TResult>` interface for all command types
2. **Type Safety**: Better compile-time type checking with generic result types
3. **Cleaner API**: Fewer interfaces to understand and implement
4. **Consistency**: Aligns with `Query<TResult>` pattern already used in the library
5. **Backward Compatibility**: Pipeline behaviors and mediator usage remain unchanged

## Checklist for Migration

- [ ] Replace `Command` implementations with `Command.Unit`
- [ ] Replace `CommandHandler<TCommand>` with `CommandHandler.Unit<TCommand>`
- [ ] Replace `CommandWithResult<TResult>` with `Command<TResult>`
- [ ] Replace `CommandWithResultHandler<TCommand, TResult>` with `CommandHandler<TCommand, TResult>`
- [ ] Update import statements to remove references to deleted interfaces
- [ ] Test all command handlers to ensure they work correctly
- [ ] Update any custom extensions or utilities that referenced the old interfaces

## Troubleshooting

### Common Compilation Errors

1. **"Unresolved reference: CommandWithResult"**
   - Replace with `Command<TResult>`

2. **"Unresolved reference: CommandWithResultHandler"**
   - Replace with `CommandHandler<TCommand, TResult>`

3. **"Type mismatch" errors on unit commands**
   - Ensure unit commands implement `Command.Unit`
   - Ensure unit handlers implement `CommandHandler.Unit<TCommand>`

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
