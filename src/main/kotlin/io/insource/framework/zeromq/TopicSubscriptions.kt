package io.insource.framework.zeromq

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.api.Context
import org.zeromq.api.Message
import org.zeromq.api.Socket
import org.zeromq.api.SocketType

/**
 * TopicSubscriptions class that manages queue bindings and a set of publisher
 * sockets used to send messages to subscribers with multiple inboxes (queues).
 */
internal class TopicSubscriptions(
  private val context: Context
) {
  private val bindings = mutableMapOf<String, MutableSet<String>>()
  private val publishers = mutableMapOf<String, Socket>()

  fun registerQueueBinding(topic: String, routingKey: String, queueId: String): Socket? {
    // Register subscription binding to queue
    bindings.computeIfAbsent("$topic-$routingKey") {
      mutableSetOf()
    } += queueId

    return if (publishers.containsKey(queueId)) {
      null
    } else {
      // Add publisher socket to lookup table
      publishers[queueId] = context.buildSocket(SocketType.PUB)
        .connect("inrpoc://$queueId")

      // Create subscriber socket as a separate inbox
      context.buildSocket(SocketType.SUB)
        .asSubscribable()
        .subscribeAll()
        .bind("inproc://$queueId")
    }
  }

  fun send(topic: String, routingKey: String, message: Message) {
    val subscriptions = bindings["$topic-$routingKey"] ?: emptySet<String>()
    if (subscriptions.isEmpty()) {
      LOGGER.trace("No subscriptions found for topic {} with routing-key {}", topic, routingKey)
      return
    }

    // Deliver message to each registered subscriber
    for (queue in subscriptions) {
      val socket = publishers[queue] ?: throw IllegalStateException("Unable to find socket for topic $topic with routing-key $routingKey on queue  $queue")

      LOGGER.trace("Resolved subscription for topic {} with routing-key {} - Delivering to queue {}", topic, routingKey, queue)
      socket.send(message)
    }
  }

  fun clear() {
    bindings.clear()
    publishers.clear()
  }

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(TopicSubscriptions::class.java)
  }
}