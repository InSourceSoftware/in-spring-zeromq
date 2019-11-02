package io.insource.framework.zeromq

import io.insource.framework.annotation.ZmqHandler
import io.insource.framework.util.AnnotationUtils.getAnnotation
import org.zeromq.api.LoopAdapter
import org.zeromq.api.Reactor
import org.zeromq.api.Socket
import java.lang.reflect.Method

/**
 * Implementation of a task for event-driven reactors that receives a message
 * and invokes a message listener with the `@ZmqHandler` annotation.
 */
class ZmqHandlerInvoker(
  private val messageListener: Any,
  private val messageConverter: MessageConverter
) : LoopAdapter() {
  private val converters = mutableMapOf<Class<*>, Method>()
  init {
    for (method in messageListener::class.java.methods) {
      val annotation = getAnnotation(method, ZmqHandler::class)
      if (annotation != null && method.parameterTypes.isNotEmpty()) {
        val targetType = method.parameterTypes[0]
        converters[targetType] = method
      }
    }
  }

  override fun execute(reactor: Reactor, socket: Socket) {
    val message = socket.receiveMessage()
    val obj = messageConverter.fromMessage(message)
    for ((targetType, method) in converters) {
      if (targetType == obj::class.java) {
        method.invoke(messageListener, obj)
      }
    }
  }
}