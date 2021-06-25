package com.trendyol

import com.trendyol.kediatr.CommandBus
import com.trendyol.kediatr.CommandBusImpl
import com.trendyol.kediatr.common.RegistryImpl
import com.trendyol.kediatr.spring.KediatrConfiguration
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
    lateinit var springBeanRegistry: RegistryImpl

    @Test
    fun contextLoads() {
        assertNotNull(commandBus)
        assert(commandBus is CommandBusImpl)
        assertNotNull(springBeanRegistry)
    }

}
