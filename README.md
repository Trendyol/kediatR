# kediatR ![Release kediatR-core](https://github.com/Trendyol/kediatR/workflows/Release%20kediatR-core/badge.svg?branch=master) ![Release kediatR-spring-starter](https://github.com/Trendyol/kediatR/workflows/Release%20kediatR-spring-starter/badge.svg?branch=master) [![codecov](https://codecov.io/gh/trendyol/kediatr/branch/master/graph/badge.svg)](https://codecov.io/gh/trendyol/kediatr)
<img align="left" alt="Humus! The kediatr mascot" src="/humus.png" alt="drawing" width="80"/>
Mediator implementation in kotlin with native coroutine support.

Supports synchronous and async (using kotlin [coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)
) command and query handling, native kotlin implementation, spring-boot, quarkus and koin configurations.

After kediatr-core version 1.0.17 you can use any dependency injection framework by implementing DependencyProvider interface.

kediatR has multiple implementations: kediatR-core, kediatR-spring-starter, kediatR-koin-starter and kediatR-quarkus-starter.

#### kediatR-core

```
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-core</artifactId>
  <version>1.0.17</version>
</dependency>
```

#### kediatR-spring-starter

```
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-spring-starter</artifactId>
  <version>1.0.17</version>
</dependency>
```

#### kediatR-koin-starter

```
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-koin-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### kediatR-quarkus-starter

```
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-quarkus-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

* add kediatr-core dependency to your POM

#### Command dispatching

```kotlin
class ManuelDependencyProvider(
    private val handlerMap: HashMap<Class<*>, Any>
) : DependencyProvider {
    override fun <T> getSingleInstanceOf(clazz: Class<T>): T {
        return handlerMap[clazz] as T
    }

    override fun <T> getSubTypesOf(clazz: Class<T>): Collection<Class<T>> {
        return handlerMap
            .filter { it.key.interfaces.contains(clazz) }
            .map { it.key as Class<T> }
    }
}

fun main() {
    val handler = MyCommandHandler()
    val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyCommandHandler::class.java, handler))
    val provider = ManuelDependencyProvider(handlers)
    val bus: CommandBus = CommandBusBuilder(provider).build()
    bus.executeCommand(HelloCommand("hello"))
}

class HelloCommand(val message: String) : Command

class HelloCommandHandler : CommandHandler<HelloCommand> {
    override fun handle(command: MyCommand) {
        println(command.message)
    }
}

```

#### Query dispatching

```kotlin

fun main() {
    val handler = GetSomeDataQueryHandler()
    val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(GetSomeDataQuery::class.java, handler))
    val provider = ManuelDependencyProvider(handlers)
    val bus: CommandBus = CommandBusBuilder(provider).build()
    val result: String = bus.executeQuery(GetSomeDataQuery(1))
    println(result)
}

class GetSomeDataQuery(val id: Int) : Query<String>

class GetSomeDataQueryHandler : QueryHandler<GetSomeDataQuery, String> {
    override fun handle(query: GetSomeDataQuery): String {
        // you can use properties in the query object to retrieve data from somewhere
        // val result = getDataFromSomewhere(query.id)
        // return result

        return "hello"
    }
}
```

#### Pipeline Behavior

```kotlin
class CommandProcessingPipeline : PipelineBehavior {
    override fun <TRequest> preProcess(request: TRequest) {
        println("Starting process.")
    }
    override fun <TRequest> postProcess(request: TRequest) {
        println("Ending process.")
    }
    override fun <TRequest, TException : Exception> handleExceptionProcess(request: TRequest, exception: TException) {
        println("Some exception occurred during process. Error: $exception")
    }
}
```

## Usage with SpringBoot

* add kediatr-spring dependency to your POM and enjoy yourself

```kotlin

@Service
class UserService(private val commandBus: CommandBus) {
    fun findUser(id: Long) {
        return commandBus.executeQuery(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long) : Query<UserDto>

@Component
class GetUserByIdQueryHandler(private val userRepository: UserRepository) : QueryHandler<GetUserByIdQuery, UserDto> {
    fun handle(query: GetUserByIdQuery): UserDto {
        val user = userRepository.findById(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}
```

## Async Usage with Kotlin Coroutine Support

```kotlin

class UserService(private val commandBus: CommandBus) {
    suspend fun findUser(id: Long) {
        return commandBus.executeQueryAsync(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long) : Query<UserDto>

class GetUserByIdQueryHandler(private val userRepository: UserRepository) : AsyncQueryHandler<GetUserByIdQuery, UserDto> {
    suspend fun handleAsync(query: GetUserByIdQuery): UserDto {
        val user = userRepository.findByIdAsync(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}

class AsyncCommandProcessingPipeline : AsyncPipelineBehavior {
    override suspend fun <TRequest> preProcess(request: TRequest) {
        println("Starting process.")
    }
    override suspend fun <TRequest> postProcess(request: TRequest) {
        println("Ending process.")
    }
    override suspend fun <TRequest, TException : Exception> handleException(request: TRequest, exception: TException) {
        println("Some exception occurred during process. Error: $exception")
    }
}
```

## Usage with Koin

Simply inject kediatr as a singleton dependency with any module and inject handler instances.
KediatrKoin.getCommandBus() must be in  the same module with at least one Handler to get correct package name for reflection.
Please note that this is an experimental release and reflection strategy with koin is a little wonky. Please open a pull request if you think there is a better implementation.

```kotlin
val kediatrModule = module {
    single { KediatrKoin.getCommandBus() }
    single { GetUserByIdQueryHandler(get()) }
}

class UserService(private val commandBus: CommandBus) {
    fun findUser(id: Long) {
        return commandBus.executeQuery(GetUserByIdQuery(id))
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

## Usage with Quarkus
* Add kediatr-quarkus-starter dependency to your POM
* Quarkus does not index 3rd party libraries unless you explicitly indicate. Add this configuration to **application.properties** file.
```yaml
  quarkus:
    index-dependency:
      kediatr:
        group-id: com.trendyol
        artifact-id: kediatr-quarkus-starter
 ```
* Add @Startup annotation for every handler so that kediatr can prepare queries and commands on beginning of the application.

```kotlin
class UserService(private val commandBus: CommandBus) {
    fun findUser(id: Long) {
        return commandBus.executeQuery(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long) : Query<UserDto>

@ApplicationScoped
@Startup
class GetUserByIdQueryHandler(private val userRepository: UserRepository) : QueryHandler<GetUserByIdQuery, UserDto> {
    fun handle(query: GetUserByIdQuery): UserDto {
        val user = userRepository.findById(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}

```

## Review Our IntelliJ Plugin

https://plugins.jetbrains.com/plugin/16017-kediatr-helper

![Screencast 1](https://plugins.jetbrains.com/files/16017/screenshot_cf56bd23-3de8-41fe-814a-64f69ae0a7c4)

![Screencast 2](https://plugins.jetbrains.com/files/16017/screenshot_c3a51b67-807c-46a1-a44c-91b6f0963aea)

Source: https://github.com/bilal-kilic/kediatr-helper 


