package io.insource.framework.zeromq

import io.insource.framework.annotation.EnableZmqListener
import io.insource.framework.annotation.EnableZmqPublisher
import io.insource.framework.annotation.EnableZmqSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableZmqSubscriber
@EnableZmqListener
@EnableZmqPublisher
@ComponentScan("io.insource.framework")
class ZeromqTestConfiguration {
  @Bean
  fun messageConverter(): MessageConverter {
    return VerySimpleMessageConverter()
  }
}