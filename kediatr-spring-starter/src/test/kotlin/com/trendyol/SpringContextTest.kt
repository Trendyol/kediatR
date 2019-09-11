package com.trendyol

import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.spring.KediatrConfiguration
import com.trendyol.kediatr.spring.SpringBeanRegistry
import com.trendyol.kediatr.spring.SpringCommandBus
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@SpringBootTest(classes = [KediatrConfiguration::class])
class SpringContextTest {

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

}
