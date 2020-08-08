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
  private val subscriptions = TopicSubscriptions(context)
  private val reactors = mutableListOf<Reactor>()

  @PostConstruct
  fun start() {
    // Find all beans annotated with @ZmqSubscriber
    val messageListeners = beanFactory.getBeansWithAnnotation(ZmqSubscriber::class).values
    for (messageListener in messageListeners) {
      LOGGER.debug("Found class {} with @{}", messageListener::class.simpleName, ZmqSubscriber::class.simpleName)

      // Create invoker based on type of message listener
      val messageListenerInvoker = if (messageListener is MessageListener) {
        MessageListenerInvoker(messageListener)
      } else {
        ZmqHandlerInvoker(messageListener, messageConverter)
      }
      val messageListenerInvokerId = messageListenerInvoker.hashCode()

      // Create single reactor thread to process messages for this subscriber
      val reactorBuilder = context.buildReactor()
      val annotation = getAnnotationByType(messageListener::class, ZmqSubscriber::class)
      for (queueBinding in annotation.values) {
        // Use @QueueBinding info to create local subscriptions
        val topic = queueBinding.topic
        val routingKey = queueBinding.key
        val queue = queueBinding.queue
        val queueId = "$queue-$messageListenerInvokerId"
        LOGGER.debug("Registering listener {} with queue binding '{}-{}' on queue '{}'", messageListener::class.simpleName, topic, routingKey, queue)

        // Create a pub/sub pair for each unique queue name
        subscriptions.registerQueueBinding(topic, routingKey, queueId)?.let {
          reactorBuilder.withInPollable(it, messageListenerInvoker)
        }
      }

      // Add one reactor per subscriber
      reactors += reactorBuilder.build()
    }

    // Look up port to bind to for this VM
    val port = zmqSubscriberProperties.port
    LOGGER.debug("Registering listener {} on port {}", TopicListener::class.simpleName, port)

    // Bind listener to configured port and start reactors
    reactors += context.listenOn(port, MessageListenerInvoker(Agent()))
    reactors.forEach(Reactor::start)
  }

  @PreDestroy
  fun stop() {
    LOGGER.debug("Destroying {}", TopicListener::class.simpleName)
    context.terminate()
    reactors.forEach(Reactor::stop)
    context.close()
    subscriptions.clear()
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

      // Forward message to subscribers
      val topic = frame.string         // Topic name
      val routingKey = frame.string    // Routing key
      LOGGER.debug("Received message for topic {} with routing-key {}", topic, routingKey)

      subscriptions.send(topic, routingKey, message)
    }
  }

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(TopicListener::class.java)
  }

  private fun <T : Annotation> ListableBeanFactory.getBeansWithAnnotation(annotationType: KClass<T>): Map<String, Any> {
    return getBeansWithAnnotation(annotationType.java)
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