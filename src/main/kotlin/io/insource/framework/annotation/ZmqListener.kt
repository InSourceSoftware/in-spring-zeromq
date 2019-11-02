package io.insource.framework.annotation

import org.springframework.stereotype.Component

/**
 * Annotation to declare a 0MQ listener which receives messages delivered on
 * a 0MQ PULL socket bound to a specified port.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ZmqListener(
  /**
   * The port to bind to using a 0MQ PULL socket.
   */
  val port: Int
)