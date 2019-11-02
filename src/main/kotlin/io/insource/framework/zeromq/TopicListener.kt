package io.insource.framework.zeromq

import io.insource.framework.annotation.ConditionalOnEnableZmqSubscriber
import io.insource.framework.annotation.ZmqSubscriber
import io.insource.framework.config.ZmqSubscriberProperties
import io.insource.framework.util.AnnotationUtils.getAnnotationByType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Component
import org.zeromq.ContextFactory
import org.zeromq.api.Context
import org.zeromq.api.LoopHandler
import org.zeromq.api.Message
import org.zeromq.api.Reactor
import org.zeromq.api.Socket
import org.zeromq.api.SocketType
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.reflect.KClass

/**
 * TopicListener class that listens to a specific port for messages containing
 * topic information, manages subscriptions, and runs an event-driven reactor
 * to deliver messages to subscribers.
 */
@Component
@ConditionalOnEnableZmqSubscriber
internal class TopicListener(
  private val zmqSubscriberProperties: ZmqSubscriberProperties,
  private val beanFactory: ListableBeanFactory,
  private val messageConverter: MessageConverter
) {
  private val context = ContextFactory.createContext(1)
  private val bindings = mutableMapOf<String, MutableSet<String>>()
  private val queues = mutableMapOf<String, Socket>()

  private val reactors = mutableListOf<Reactor>()

  @PostConstruct
  fun start() {
    val messageListeners = beanFactory.getBeansWithAnnotation(ZmqSubscriber::class).values
    for (messageListener in messageListeners) {
      LOGGER.debug("Found class {} with @{}", messageListener::class.simpleName, ZmqSubscriber::class.simpleName)

      val annotation = getAnnotationByType(messageListener::class, ZmqSubscriber::class)
      val messageListenerInvoker = if (messageListener is MessageListener) {
        MessageListenerInvoker(messageListener)
      } else {
        ZmqHandlerInvoker(messageListener, messageConverter)
      }
      val messageListenerInvokerId = messageListenerInvoker.hashCode()

      val reactorBuilder = context.buildReactor()
      for (queueBinding in annotation.values) {
        val topic = queueBinding.topic
        val routingKey = queueBinding.key
        val queue = queueBinding.queue
        val queueId = "$queue-$messageListenerInvokerId"
        LOGGER.debug("Registering listener {} with queue binding '{}-{}' on queue '{}'", messageListener::class.simpleName, topic, routingKey, queue)

        // Create a pub/sub pair for each unique queue name
        queues.computeIfAbsent(queueId) {
          LOGGER.debug("Creating inbox {}", queueId)

          // Create subscriber socket as a separate inbox
          reactorBuilder.withInPollable(context.subscribeTo(queueId), messageListenerInvoker)

          // Add publisher socket to lookup table
          context.publishTo(queueId)
        }

        // Register subscription binding to queue
        bindings.computeIfAbsent("$topic-$routingKey") {
          mutableSetOf()
        } += queueId
      }

      reactors += reactorBuilder.build()
    }

    val port = zmqSubscriberProperties.port
    LOGGER.debug("Registering listener {} on port {}", TopicListener::class.simpleName, port)

    reactors += context.listenOn(port, MessageListenerInvoker(Agent()))
    reactors.forEach(Reactor::start)
  }

  @PreDestroy
  fun stop() {
    LOGGER.debug("Destroying {}", TopicListener::class.simpleName)
    context.terminate()
    reactors.forEach(Reactor::stop)
    context.close()

    queues.clear()
    bindings.clear()
  }

  private inner class Agent : MessageListener {
    override fun onMessage(message: Message) {
      val frame = message.popFrame()   // Topic info
      val version = frame.byte.toInt() // Message version

      // Check message version
      if (version != 1) {
        LOGGER.warn("Invalid message version: $version - Ignoring message.")
        return
      }

      val topic = frame.string         // Topic name
      val routingKey = frame.string    // Routing key
      LOGGER.debug("Received message for topic {} with routing-key {}", topic, routingKey)

      val subscriptions = bindings["$topic-$routingKey"] ?: emptySet<String>()
      if (subscriptions.isEmpty()) {
        LOGGER.trace("No subscriptions found for topic {} with routing-key {}", topic, routingKey)
        return
      }

      for (queue in subscriptions) {
        LOGGER.trace("Resolved subscription for topic {} with routing-key {} - Delivering to queue {}", topic, routingKey, queue)
        queues[queue]!!.send(message)
      }
    }
  }

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(TopicListener::class.java)
  }

  private fun <T : Annotation> ListableBeanFactory.getBeansWithAnnotation(annotationType: KClass<T>): MutableMap<String, Any> {
    return getBeansWithAnnotation(annotationType.java)
  }

  private fun Context.subscribeTo(queueId: String): Socket {
    return buildSocket(SocketType.SUB)
      .asSubscribable()
      .subscribeAll()
      .bind("inproc://$queueId")
  }

  private fun Context.publishTo(queueId: String): Socket {
    return buildSocket(SocketType.PUB).connect("inproc://$queueId")
  }

  private fun Context.listenOn(port: Int, handler: LoopHandler): Reactor {
    val socket = buildSocket(SocketType.SUB)
      .asSubscribable()
      .subscribeAll()
      .bind("tcp://*:$port")

    return buildReactor()
      .withInPollable(socket, handler)
      .build()
  }
}