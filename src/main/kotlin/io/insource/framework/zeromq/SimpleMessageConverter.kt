package io.insource.framework.zeromq

import org.springframework.stereotype.Component
import org.zeromq.api.Message

/**
 * Simple message converter that simply checks the type of the provided object
 * for conversion to a message using the `toMessage()` method, and relies on the
 * the first frame containg headers with a `content-type` header to determine
 * the behavior of the `fromMessage()` method.
 */
@Component
class SimpleMessageConverter : MessageConverter {
  override fun toMessage(obj: Any, headers: Map<String, String>): Message {
    val contentType: String
    val payloadFrame = when (obj) {
      is ByteArray -> {
        contentType = APPLICATION_OCTET_STREAM
        Message.Frame.of(obj)
      }
      is String -> {
        contentType = TEXT_PLAIN
        Message.Frame.of(obj)
      }
      else -> throw IllegalArgumentException("${this.javaClass.simpleName} only supports ByteArray or String types in the payload, received ${obj.javaClass.simpleName}")
    }

    val updatedHeaders = HashMap(headers)
    updatedHeaders["content-type"] = contentType
    updatedHeaders["content-length"] = payloadFrame.size().toString()

    return Message()
      .addFrame(Message.Frame.of(updatedHeaders))
      .addFrame(payloadFrame)
  }

  override fun fromMessage(message: Message): Any {
    val headers = message.popMap()
    return when (val contentType = headers["content-type"] ?: APPLICATION_OCTET_STREAM) {
      TEXT,
      TEXT_PLAIN,
      APPLICATION_JSON,
      APPLICATION_XML -> return message.popString()
      APPLICATION_OCTET_STREAM -> message.popBytes()
      else -> throw IllegalArgumentException("Unsupported content-type - $contentType")
    }
  }

  companion object {
    private val TEXT = "text"
    private val TEXT_PLAIN = "text/plain"
    private val APPLICATION_JSON = "application/json"
    private val APPLICATION_XML = "application/xml"
    private val APPLICATION_OCTET_STREAM = "application/octet-stream"
  }
}