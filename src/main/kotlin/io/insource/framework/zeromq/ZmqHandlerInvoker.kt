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
internal class ZmqHandlerInvoker(
  private val messageListener: Any,
  private val messageConverter: MessageConverter
) : LoopAdapter() {
  private val converters = mutableMapOf<Class<*>, Method>()

  init {
    // Find methods on message listener with @ZmqHandler annotation
    for (method in messageListener::class.java.methods) {
      val annotation = getAnnotation(method, ZmqHandler::class)
      // Find methods that have one parameter and record the type
      if (annotation != null && method.parameterTypes.size == 1) {
        val targetType = method.parameterTypes[0]
        converters[targetType] = method
      }
    }
  }

  override fun execute(reactor: Reactor, socket: Socket) {
    val message = socket.receiveMessage()
    val obj = messageConverter.fromMessage(message)

    // Look for handler method whose method signature matches
    for ((targetType, method) in converters) {
      if (targetType.isAssignableFrom(obj::class.java)) {
        method.invoke(messageListener, obj)
      }
    }
  }
}