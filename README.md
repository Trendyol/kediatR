# kediatR ![Release kediatR-core](https://github.com/Trendyol/kediatR/workflows/Release%20kediatR-core/badge.svg?branch=master) ![Release kediatR-spring-starter](https://github.com/Trendyol/kediatR/workflows/Release%20kediatR-spring-starter/badge.svg?branch=master) [![codecov](https://codecov.io/gh/trendyol/kediatr/branch/v2.0/graph/badge.svg)](https://codecov.io/gh/trendyol/kediatr)

<img style="float: left" alt="Humus! The kediatr mascot" src="/humus.png" alt="drawing" width="80"/>

Mediator implementation in kotlin with native coroutine support. Supports Spring-Boot, Quarkus and Koin dependency proviers.

## Usage

```diff
+ $version = 2.0.0
```

<details open>
<summary>Gradle</summary>

To use the SNAPSHOT version you need to add repository to your dependency management:

```kotlin
 maven {
     url = uri("https://oss.sonatype.org/content/repositories/snapshots")
 }
```

#### kediatR-core

```kotlin
 implementation("com.trendyol:kediatr-core:$version")
```

#### kediatR-spring-starter

```kotlin
 implementation("com.trendyol:kediatr-spring-starter:$version")
```

#### kediatR-koin-starter

```kotlin
 implementation("com.trendyol:kediatr-koin-starter:$version")
```

#### kediatR-quarkus-starter

```kotlin
 implementation("com.trendyol:kediatr-quarkus-starter:$version")
```

</details>

<details>
<summary>Maven</summary>

To use the SNAPSHOT version you need to add repository to your dependency management:

```xml
<profiles>
  <profile>
     <id>allow-snapshots</id>
        <activation><activeByDefault>true</activeByDefault></activation>
     <repositories>
       <repository>
         <id>snapshots-repo</id>
         <url>https://oss.sonatype.org/content/repositories/snapshots</url>
         <releases><enabled>false</enabled></releases>
         <snapshots><enabled>true</enabled></snapshots>
       </repository>
     </repositories>
   </profile>
</profiles>
```

**kediatR-core**

```xml
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-core</artifactId>
  <version>$version</version>
</dependency>
```

**kediatR-spring-starter**

```xml
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-spring-starter</artifactId>
  <version>$version</version>
</dependency>
```

**kediatR-koin-starter**

```xml
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-koin-starter</artifactId>
  <version>$version</version>
</dependency>
```

**kediatR**-quarkus-starter

```xml
<dependency>
  <groupId>com.trendyol</groupId>
  <artifactId>kediatr-quarkus-starter</artifactId>
  <version>$version</version>
</dependency>
```

</details>

### Command dispatching

```kotlin
class ManualDependencyProvider(
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
    val handler = HelloCommandHandler()
    val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(HelloCommandHandler::class.java, handler))
    val provider = ManualDependencyProvider(handlers)
    val mediator: Mediator = MediatorBuilder(provider).build()
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

fun main() {
    val handler = GetSomeDataQueryHandler()
    val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(GetSomeDataQuery::class.java, handler))
    val provider = ManualDependencyProvider(handlers)
    val mediator: Mediator = MediatorBuilder(provider).build()
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

* Add @Startup annotation for every handler so that KediatR can prepare queries and commands on beginning of the
  application.

```kotlin
class UserService(private val mediator: mediator) {
    fun findUser(id: Long) {
        return mediator.send(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long) : Query<UserDto>

@ApplicationScoped
@Startup
class GetUserByIdQueryHandler(private val userRepository: UserRepository) : QueryHandler<GetUserByIdQuery, UserDto> {
    override suspend fun handle(query: GetUserByIdQuery): UserDto {
        val user = userRepository.findById(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}

```

## Review Our IntelliJ Plugin

<https://plugins.jetbrains.com/plugin/16017-kediatr-helper>

![Screencast 1](https://plugins.jetbrains.com/files/16017/screenshot_cf56bd23-3de8-41fe-814a-64f69ae0a7c4)

![Screencast 2](https://plugins.jetbrains.com/files/16017/screenshot_c3a51b67-807c-46a1-a44c-91b6f0963aea)

Source: <https://github.com/bilal-kilic/kediatr-helper>
