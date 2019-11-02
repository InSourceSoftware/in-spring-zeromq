package io.insource.framework.annotation

import org.springframework.stereotype.Component

/**
 * Annotation to declare a 0MQ subscriber which receives messages delivered
 * on a 0MQ PULL socket bound to a port (default 1337).
 *
 * Messages sent to this port must have a frame containing the topic name and
 * routing key that subscribers can use to declare bindings delivered to queues.
 *
 * Messages are delivered to subscribers using the publish/subscribe pattern on
 * a 0MQ SUB socket.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class ZmqSubscriber(
  /**
   * One or more queue bindings to declare on this subscriber.
   */
  vararg val values: QueueBinding
)