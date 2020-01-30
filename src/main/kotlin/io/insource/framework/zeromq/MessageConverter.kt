package io.insource.framework.zeromq

import org.zeromq.api.Message

/**
 * Interface implemented by components that convert messages into objects.
 */
interface MessageConverter {
  /**
   * Convert an object to a Message.
   *
   * @param obj The object to convert
   * @param headers Message headers to go with the payload
   * @return The message
   */
  fun toMessage(obj: Any, headers: Map<String, String>): Message

  /**
   * Convert from a message to an object.
   *
   * @param message The message to convert
   * @return The converted object
   */
  fun fromMessage(message: Message): Any
}