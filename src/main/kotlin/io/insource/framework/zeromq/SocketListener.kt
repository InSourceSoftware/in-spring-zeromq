package io.insource.framework.zeromq

import io.insource.framework.annotation.ConditionalOnEnableZmqListener
import io.insource.framework.annotation.ZmqListener
import io.insource.framework.util.AnnotationUtils.getAnnotationByType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Component
import org.zeromq.ContextFactory
import org.zeromq.api.Reactor
import org.zeromq.api.SocketType
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * SocketListener class that runs in a separate thread as an event-driven reactor
 * and delivers messages to message listeners.
 */
@Component
@ConditionalOnEnableZmqListener
internal class SocketListener(
  private val beanFactory: ListableBeanFactory,
  private val messageConverter: MessageConverter
) {
  private val context = ContextFactory.createContext(1)
  private val reactors = mutableListOf<Reactor>()

  @PostConstruct
  fun start() {
    val messageListeners = beanFactory.getBeansWithAnnotation(ZmqListener::class.java).values
    for (messageListener in messageListeners) {
      LOGGER.debug("Found class {} with @{}", messageListener::class.simpleName, ZmqListener::class.simpleName)

      val annotation = getAnnotationByType(messageListener::class, ZmqListener::class)
      val port = annotation.port
      LOGGER.debug("Registering listener {} on port {}", messageListener::class.simpleName, port)

      val socket = context.buildSocket(SocketType.PULL).bind("tcp://*:$port")
      val messageListenerInvoker = if (messageListener is MessageListener) {
        MessageListenerInvoker(messageListener)
      } else {
        ZmqHandlerInvoker(messageListener, messageConverter)
      }

      reactors += context.buildReactor()
        .withInPollable(socket, messageListenerInvoker)
        .build()
    }

    reactors.forEach(Reactor::start)
  }

  @PreDestroy
  fun stop() {
    context.terminate()
    reactors.forEach(Reactor::stop)
    context.close()
  }

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(SocketListener::class.java)
  }
}