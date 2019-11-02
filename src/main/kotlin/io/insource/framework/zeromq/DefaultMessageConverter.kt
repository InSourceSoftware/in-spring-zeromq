package io.insource.framework.zeromq

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import org.zeromq.api.Message

/**
 * Default message converter that simply converts messages to/from a string.
 * Useful in demonstrations and hello world applications.
 */
@Component
@ConditionalOnMissingBean(MessageConverter::class)
class DefaultMessageConverter : MessageConverter {
  override fun toMessage(obj: Any): Message {
    return Message(obj.toString())
  }

  override fun fromMessage(message: Message): Any {
    return message.popString()
  }
}