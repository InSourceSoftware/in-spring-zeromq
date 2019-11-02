package io.insource.framework.zeromq

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.api.LoopAdapter
import org.zeromq.api.Reactor
import org.zeromq.api.Socket

/**
 * Implementation of a task for event-driven reactors that receives a message
 * and invokes a message listener that implements the `MessageListener` interface.
 */
internal class MessageListenerInvoker(private val messageListener: MessageListener) : LoopAdapter() {
  override fun execute(reactor: Reactor, socket: Socket) {
    try {
      LOGGER.trace("Invoking {} on {} with message", MessageListener::class.simpleName, messageListener::class.simpleName)
      messageListener.onMessage(socket.receiveMessage())
    } catch (t: Throwable) {
      LOGGER.error("Unexpected error while processing message on ${messageListener::class.simpleName}", t)
    }
  }

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(MessageListenerInvoker::class.java)
  }
}