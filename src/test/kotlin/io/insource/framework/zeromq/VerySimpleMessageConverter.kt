package io.insource.framework.zeromq

import org.zeromq.api.Message

class VerySimpleMessageConverter : MessageConverter {
  override fun toMessage(obj: Any, headers: Map<String, String>): Message {
    return Message(obj.toString())
  }

  override fun fromMessage(message: Message): Any {
    return message.popString()
  }
}