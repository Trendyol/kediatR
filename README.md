# kediatR ![Release kediatR-core](https://github.com/Trendyol/kediatR/workflows/Release%20kediatR-core/badge.svg?branch=master) ![Release kediatR-spring-starter](https://github.com/Trendyol/kediatR/workflows/Release%20kediatR-spring-starter/badge.svg?branch=master) [![codecov](https://codecov.io/gh/trendyol/kediatr/branch/master/graph/badge.svg)](https://codecov.io/gh/trendyol/kediatr)

Mediator implementation in kotlin with native coroutine support.

Supports synchronous and async (using kotlin [coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)
) command and query handling, native kotlin implementation and spring-boot configuration

kediatR has two implementations: kediatR-core and kediatR-spring 

#### kediatR-core
```
<dependency>
    <groupId>com.trendyol</groupId>
    <artifactId>kediatr-core</artifactId>
    <version>1.0.13</version>
</dependency>
```

#### kediatR-spring-starter 
```
<dependency>
    <groupId>com.trendyol</groupId>
    <artifactId>kediatr-spring-starter</artifactId>
    <version>1.0.14</version>
</dependency>
```

## Usage
* add kediatr-core dependency to your POM
#### Command dispatching
```kotlin

fun main(){
    // pass any class in package which contains handlers to CommandBusBuilder. 
    // So, kediatR may scans your package and registers command and query handlers 
    val bus: CommandBus = CommandBusBuilder(HelloCommand::class.java).build()
    bus.executeCommand(HelloCommand("hello"))
}

class HelloCommand(val message: String) : Command

class HelloCommandHandler: CommandHandler<HelloCommand>{
    override fun handle(command: MyCommand) {
        println(command.message)
    }
}

```
#### Query dispatching
```kotlin

fun main(){
    val bus: CommandBus = CommandBusBuilder(GetSomeDataQuery::class.java).build()
    val result: String = bus.executeQuery(GetSomeDataQuery(1))
    println(result)
}

class GetSomeDataQuery(val id: Int) : Query<String>

class GetSomeDataQueryHandler: QueryHandler<String, GetSomeDataQuery> {
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

## Usage with Spring
* add kediatr-spring dependency to your POM and enjoy yourself

```kotlin

@Service
class UserService(private val commandBus: CommandBus){
    fun findUser(id: Long){
        return commandBus.executeQuery(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long): Query<UserDto>

@Component
class GetUserByIdQueryHandler(private val userRepository: UserRepository) : QueryHandler<UserDto, GetUserByIdQuery> {
    fun handle(query: GetUserByIdQuery): UserDto { 
        val user = userRepository.findById(query.id)
        // do some operation on user
        return UserDto(user.id, user.name, user.surname)
    }
}
```

## Async Usage with Kotlin Coroutine Support
```kotlin
 
class UserService(private val commandBus: CommandBus ){
    suspend fun findUser(id: Long){
        return commandBus.executeQueryAsync(GetUserByIdQuery(id))
    }
}

class GetUserByIdQuery(private val id: Long): Query<UserDto>

class GetUserByIdQueryHandler(private val userRepository: UserRepository) : AsyncQueryHandler<UserDto, GetUserByIdQuery> {
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
