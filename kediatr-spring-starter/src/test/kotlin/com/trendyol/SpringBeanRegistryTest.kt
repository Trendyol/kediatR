package com.trendyol

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.Registry
import com.trendyol.kediatr.spring.HandlerBeanNotFoundException
import com.trendyol.kediatr.spring.KediatrConfiguration
import com.trendyol.kediatr.spring.SpringBeanRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [SpringBeanRegistry::class, RegisteredCommandHandler::class])
class SpringBeanRegistryTest {
    @Autowired
    lateinit var registry: Registry

    @Test
    fun `registry should throw HandlerBeanNotFoundException fi given handler could not be found in spring application context`() {
        val exception: HandlerBeanNotFoundException = assertFailsWith {
            registry.resolveCommandHandler(NonRegisteredMyCommand().javaClass)
        }

        assertNotNull(exception)
    }

    @Test
    fun `registry should handler`() {
        val handler = registry.resolveCommandHandler(RegisteredMyCommand().javaClass)

        assertNotNull(handler)
    }
}

class NonRegisteredMyCommand : Command

class NonRegisteredCommandHandler : CommandHandler<NonRegisteredMyCommand> {
    override fun handle(command: NonRegisteredMyCommand) {
    }
}

class RegisteredMyCommand : Command

class RegisteredCommandHandler : CommandHandler<RegisteredMyCommand> {
    override fun handle(command: RegisteredMyCommand) {
    }
}
