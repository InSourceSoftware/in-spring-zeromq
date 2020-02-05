package io.insource.framework.config

import io.insource.framework.annotation.ConditionalOnEnableZmqPublisher
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
@ConditionalOnEnableZmqPublisher
@ConfigurationProperties("zmq.publisher")
internal class ZmqPublisherProperties {
  /**
   * The remote 0MQ host.
   */
  var host: String = "localhost"

  /**
   * The port of the remote 0MQ host.
   */
  var port: Int = 1337

  /**
   * A list of topics to bind to within this application instance (JVM).
   */
  var topics = listOf("events")
}