package com.trendyol

import com.trendyol.kediatr.AsyncCommandWithResultHandler
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusBuilder
import com.trendyol.kediatr.CommandWithResult
import com.trendyol.kediatr.CommandWithResultHandler
import com.trendyol.kediatr.HandlerNotFoundException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private var counter = 0
private var asyncTestCounter = 0

class CommandWithResultHandlerTest {

    init {
        counter = 0
        asyncTestCounter = 0
    }

    @Test
    fun `commandHandler should be fired`() {
        val handler = MyCommandRHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(MyCommandRHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommand(MyCommandR())

        assertTrue {
            counter == 1
        }
    }

    @Test
    fun `async commandHandler should be fired`() = runBlocking {
        val handler = AsyncMyCommandRHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AsyncMyCommandRHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyAsyncCommandR())

        assertTrue {
            asyncTestCounter == 1
        }
    }

    @Test
    fun `should throw exception if given async command has not been registered before`() {
        val provider = ManualDependencyProvider(hashMapOf())
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runBlocking {
                bus.executeCommandAsync(NonExistCommandR())
            }
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommandR")
    }

    @Test
    fun `should throw exception if given command has not been registered before`() {
        val provider = ManualDependencyProvider(hashMapOf())
        val bus: CommandBus = CommandBusBuilder(provider).build()
        val exception = assertFailsWith(HandlerNotFoundException::class) {
            bus.executeCommand(NonExistCommandR())
        }

        assertNotNull(exception)
        assertEquals(exception.message, "handler could not be found for com.trendyol.NonExistCommandR")
    }

    @Test
    fun inheritance_should_work() = runBlocking {
        var invocationCount = 0

        class MyAsyncCommand : CommandWithResult<Result>

        class AsyncMyCommandHandler : AsyncCommandWithResultHandler<MyAsyncCommand, Result> {
            override suspend fun handleAsync(command: MyAsyncCommand): Result {
                invocationCount++
                return Result()
            }
        }

        val handler = AsyncMyCommandHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(AsyncMyCommandHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: CommandBus = CommandBusBuilder(provider).build()
        bus.executeCommandAsync(MyAsyncCommand())

        assertEquals(1, invocationCount)
    }

    @Nested
    inner class ParamaterizedTests {
        init {
            counter = 0
            asyncTestCounter = 0
        }

        inner class ParameterizedCommandWithResult<TParam>(val param: TParam) : CommandWithResult<String>

        inner class ParatemerizedAsyncCommandWithResultHandler<TParam> :
            AsyncCommandWithResultHandler<ParameterizedCommandWithResult<TParam>, String> {
            override suspend fun handleAsync(command: ParameterizedCommandWithResult<TParam>): String {
                counter++
                return command.param.toString()
            }
        }

        inner class ParameterizedCommandWithResultHandler<TParam> :
            CommandWithResultHandler<ParameterizedCommandWithResult<TParam>, String> {
            override fun handle(command: ParameterizedCommandWithResult<TParam>): String {
                counter++
                return command.param.toString()
            }
        }

        @Test
        fun `async commandWithResult should be fired and return result`() = runBlocking {
            // given
            val handler = ParatemerizedAsyncCommandWithResultHandler<ParameterizedCommandWithResult<Long>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParatemerizedAsyncCommandWithResultHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            val result = bus.executeCommandAsync(ParameterizedCommandWithResult(61L))

            // then
            assertTrue { counter == 1 }
            assertEquals(result, "61")
        }

        @Test
        fun `commandWithResult should be fired and return result`() {
            // given
            val handler = ParameterizedCommandWithResultHandler<ParameterizedCommandWithResult<Long>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(ParameterizedCommandWithResultHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            val result = bus.executeCommand(ParameterizedCommandWithResult(61L))

            // then
            assertTrue { counter == 1 }
            assertEquals(result, "61")
        }

        @Test
        fun inheritance_should_work() = runBlocking {
            var invocationCount = 0

            class ParameterizedCommandWithResult<TParam>(val param: TParam) : CommandWithResult<String>

            abstract class ParameterizedCommandWithResultHandlerBase<TParam : CommandWithResult<String>> :
                AsyncCommandWithResultHandler<TParam, String>

            class Handler<TParam> : ParameterizedCommandWithResultHandlerBase<ParameterizedCommandWithResult<TParam>>() {
                override suspend fun handleAsync(command: ParameterizedCommandWithResult<TParam>): String {
                    invocationCount++
                    return command.param.toString()
                }
            }

            val handler = Handler<ParameterizedCommandWithResult<Long>>()
            val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(Handler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: CommandBus = CommandBusBuilder(provider).build()

            // when
            val result = bus.executeCommandAsync(ParameterizedCommandWithResult("invoked"))

            // then
            assertEquals(1, invocationCount)
            assertEquals(result, "invoked")
        }
    }
}

private class Result
private class NonExistCommandR : Command
private class MyCommandR : CommandWithResult<Result>

private class MyCommandRHandler : CommandWithResultHandler<MyCommandR, Result> {
    override fun handle(command: MyCommandR): Result {
        counter++

        return Result()
    }
}

private class MyAsyncCommandR : CommandWithResult<Result>

private class AsyncMyCommandRHandler : AsyncCommandWithResultHandler<MyAsyncCommandR, Result> {
    override suspend fun handleAsync(command: MyAsyncCommandR): Result {
        delay(500)
        asyncTestCounter++

        return Result()
    }
}
