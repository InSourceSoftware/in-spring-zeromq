package io.insource.framework.zeromq

import org.zeromq.ContextFactory
import org.zeromq.api.Context
import org.zeromq.api.SocketType

/**
 * Helper class that simplifies synchronous ZeroMQ access (sending and receiving messages)
 * using socket-per-thread semantics.
 *
 * @param context The 0MQ context
 */
class ZmqTemplate(private val context: Context) {
  /** Default constructor. */
  constructor() : this(ContextFactory.context())

  /** Default topic name. Cannot be changed once threads have begun sending messages. */
  var topic: String = "default"

  /** Default routing key, used when the routing key is not specified. */
  var routingKey: String = ""

  /** Message converter used to convert a payload to/from a 0MQ `Message`. */
  var messageConverter: MessageConverter = SimpleMessageConverter()

  /** ThreadLocal for creating channels using socket-per-thread semantics. */
  private val channels: ThreadLocal<Channel> = ThreadLocal.withInitial {
    Channel(context.buildSocket(SocketType.PUSH).connect("inproc://$topic"), topic)
  }

  /**
   * Send a message with no headers to the default topic using the default
   * routing key.
   *
   * @param obj The message payload
   */
  fun send(obj: Any) {
    send(routingKey, obj)
  }

  /**
   * Send a message with no headers to the default topic.
   *
   * @param obj The message payload
   */
  fun send(routingKey: String, obj: Any) {
    send(routingKey, obj, mapOf())
  }

  /**
   * Send a message to the default topic.
   *
   * @param obj The message payload
   * @param headers Message headers to go with the payload
   */
  fun send(routingKey: String, obj: Any, headers: Map<String, String>) {
    val channel = channels.get()
    val message = messageConverter.toMessage(obj, headers)
    channel.send(routingKey, message)
  }
}