package io.insource.framework.annotation

/**
 * Declares a queue binding with a unique topic name and routing key combination.
 *
 * Each unique queue name specified in one or more bindings receives a separate
 * 0MQ PUB socket and its own inbox.
 */
annotation class QueueBinding(
  /**
   * The topic name used to identify a grouped stream of messages.
   */
  val topic: String,

  /**
   * The routing key used to identify messages within a topic.
   */
  val key: String,

  /**
   * The name of a queue used to identify an inbox for a subscriber.
   */
  val queue: String
)