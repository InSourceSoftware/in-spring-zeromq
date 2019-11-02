package io.insource.framework.config

import io.insource.framework.annotation.ConditionalOnEnableZmqSubscriber
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
@ConditionalOnEnableZmqSubscriber
@ConfigurationProperties("zmq.subscriber")
internal class ZmqSubscriberProperties {
  /**
   * The port of the remote 0MQ host.
   */
  var port: Int = 1337
}