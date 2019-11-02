package io.insource.framework.config

import io.insource.framework.annotation.ConditionalOnEnableZmqPublisher
import io.insource.framework.zeromq.Channels
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnEnableZmqPublisher
internal class ZmqPublisherConfiguration(
  private val zmqPublisherProperties: ZmqPublisherProperties
) {
  @Bean
  internal fun channelProxy(): Channels {
    return Channels
      .create(zmqPublisherProperties.topics)
      .connect(zmqPublisherProperties.host, zmqPublisherProperties.port)
  }
}