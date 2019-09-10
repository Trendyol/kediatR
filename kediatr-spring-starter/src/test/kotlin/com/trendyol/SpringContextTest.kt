package com.trendyol

import com.trendyol.kediatr.AsyncCommandHandler
import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.spring.HandlerBeanNotFoundException
import com.trendyol.kediatr.spring.KediatrConfiguration
import com.trendyol.kediatr.spring.SpringBeanRegistry
import com.trendyol.kediatr.spring.SpringCommandBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

var springTestCounter = 0
var springAsyncTestCounter = 0

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [KediatrConfiguration::class, MyAsyncCommandHandler::class, MyCommandHandler::class])
class SpringContextTest {

    init {
        springTestCounter = 0
        springAsyncTestCounter = 0
    }

    @Autowired
    lateinit var commandBus: CommandBus

    @Autowired
    lateinit var springBeanRegistry: SpringBeanRegistry

    @Test
    fun contextLoads() {
        assertNotNull(commandBus)
        assert(commandBus is SpringCommandBus)
        assertNotNull(springBeanRegistry)
    }

    @Test
    fun `commandHandler should be fired`() {
        commandBus.executeCommand(MyCommand())
        assertTrue {
            springTestCounter == 1
        }
    }

    @Test
    fun `async commandHandler should be fired`() {
        runBlocking {
            commandBus.executeCommandAsync(MyCommand()).join()

            assertTrue {
                springAsyncTestCounter == 1
            }
        }
    }

    @Test
    fun `commandBus should throw HandlerBeanNotFoundException `() {
        val exception: HandlerBeanNotFoundException = assertFailsWith {
            commandBus.executeCommand(MyCommand2())
        }

        assertNotNull(exception)
    }

}

class MyCommand : Command

class MyCommandHandler : CommandHandler<MyCommand> {
    override fun handle(command: MyCommand) {
        springTestCounter++
    }
}

class MyAsyncCommandHandler : AsyncCommandHandler<MyCommand> {
    override suspend fun handleAsync(command: MyCommand) {
        delay(500)
        springAsyncTestCounter++
    }
}

class MyCommand2 : Command

class NonRegisteredCommandHandler : CommandHandler<MyCommand2> {
    override fun handle(command: MyCommand2) {
        springTestCounter++
    }
}

