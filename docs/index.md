# kediatR

Mediator implementation for Kotlin.

## Usage

```diff
+ $version = 3.1.1
```

<details open>
<summary>Gradle</summary>

kediatR-core

```kotlin
 implementation("com.trendyol:kediatr-core:$version")
```

kediatR provides two different packages for spring-boot 2x and 3x. You can use the following dependencies according to
your spring-boot version.

```kotlin
 implementation("com.trendyol:kediatr-spring-boot-2x-starter:$version")

  // or

 implementation("com.trendyol:kediatr-spring-boot-3x-starter:$version")
```

kediatR-koin-starter

```kotlin
 implementation("com.trendyol:kediatr-koin-starter:$version")
```

kediatR-quarkus-starter

```kotlin
 implementation("com.trendyol:kediatr-quarkus-starter:$version")
```

</details>

### Command dispatching

```kotlin
import com.trendyol.kediatr.MappingDependencyProvider.Companion.createMediator

fun main() {
    val handler = HelloCommandHandler()
    val mediator: Mediator = createMediator(handlers = listOf(handler))
    mediator.send(HelloCommand("hello"))
}

class HelloCommand(val message: String) : Command

class HelloCommandHandler : CommandHandler<HelloCommand> {
    override suspend fun handle(command: HelloCommand) {
        println(command.message)
    }
}
```

### Query dispatching

```kotlin
import com.trendyol.kediatr.MappingDependencyProvider.Companion.createMediator

fun main() {
    val handler = GetSomeDataQueryHandler()
    val mediator: Mediator = createMediator(handlers = listOf(handler))
    val result: String = mediator.send(GetSomeDataQuery(1))
    println(result)
}

class GetSomeDataQuery(val id: Int) : Query<String>

class GetSomeDataQueryHandler : QueryHandler<GetSomeDataQuery, String> {
    override suspend fun handle(query: GetSomeDataQuery): String {
        // you can use properties in the query object to retrieve data from somewhere
        // val result = getDataFromSomewhere(query.id)
        // return result

        return "hello"
    }
}
```

### Pipeline Behavior

```kotlin
class CommandProcessingPipeline : PipelineBehavior {
  
    override val order: Int = 1
  
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: RequestHandlerDelegate<TRequest, TResponse>
    ): TResponse {
        println("Starting process.")
        val result = next(request)
        println("Ending process.")
        return result
    }
}
```

### SpringBoot

* Add _kediatr-spring_ dependency to your maven or gradle dependencies

```kotlin
@Service
class UserService(private val mediator: Mediator) {
    suspend fun findUser(id: Long) {
        return mediator.send(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long) : Query<UserDto>

@Component
class GetUserByIdQueryHandler(private val userRepository: UserRepository): QueryHandler<GetUserByIdQuery, UserDto> {
    override suspend fun handle(query: GetUserByIdQuery): UserDto {
        val user = userRepository.findById(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}
```

## Koin

Simply inject KediatR as a singleton dependency with any module and inject handler instances.
KediatRKoin.getMediator() must be in the same module with at least one Handler to get correct package name for
reflection.
Please note that this is an experimental release and reflection strategy with koin is a little wonky. Please open a pull
request if you think there is a better implementation.

```kotlin
val kediatRModule = module {
    single { KediatRKoin.getMediator() }
    single { GetUserByIdQueryHandler(get()) }
}

class UserService(private val mediator: Mediator) {
    fun findUser(id: Long) {
        return mediator.send(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long) : Query<UserDto>

class GetUserByIdQueryHandler(private val userRepository: UserRepository) : QueryHandler<GetUserByIdQuery, UserDto> {
    fun handle(query: GetUserByIdQuery): UserDto {
        val user = userRepository.findById(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}
```

## Quarkus

* Add _kediatr-quarkus-starter_ dependency to your dependencies
* Quarkus does not index 3rd party libraries unless you explicitly indicate. Add this configuration to **
  application.properties** file.

```yaml
  quarkus:
    index-dependency:
      kediatr:
        group-id: com.trendyol
        artifact-id: kediatr-quarkus-starter
```

```kotlin
class UserService(private val mediator: mediator) {
    fun findUser(id: Long) {
        return mediator.send(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long) : Query<UserDto>

@ApplicationScoped
class GetUserByIdQueryHandler(private val userRepository: UserRepository) : QueryHandler<GetUserByIdQuery, UserDto> {
    override suspend fun handle(query: GetUserByIdQuery): UserDto {
        val user = userRepository.findById(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}
```

## Check Our IntelliJ Plugin

<https://plugins.jetbrains.com/plugin/16017-kediatr-helper>

![Screencast 1](https://plugins.jetbrains.com/files/16017/screenshot_cf56bd23-3de8-41fe-814a-64f69ae0a7c4)

![Screencast 2](https://plugins.jetbrains.com/files/16017/screenshot_c3a51b67-807c-46a1-a44c-91b6f0963aea)

Source: <https://github.com/bilal-kilic/kediatr-helper>


