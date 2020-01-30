package io.insource.framework.zeromq

import org.zeromq.api.Message
import org.zeromq.api.Socket
import java.io.Closeable

/**
 * Message channel for sending messages to a topic using a routing key.
 */
class Channel internal constructor(
  private val socket: Socket,
  private val topic: String
) : Closeable {
  /**
   * Send a message to this channel's topic using a routing key.
   *
   * @param routingKey The routing key used to identify messages within a topic
   * @param message The message to send
   */
  fun send(routingKey: String, message: Message) {
    // Encode topic info as first frame
    val topicInfo = Message.FrameBuilder(48)
      .putByte(1)
      .putString(topic)
      .putString(routingKey)
      .build()

    // Build and send message
    val frames = Message()
      .addFrame(topicInfo)
      .addFrames(message)
    socket.send(frames)
  }

  /**
   * Close the underlying socket manually. Note: This step is optional.
   */
  override fun close() {
    socket.close()
  }
}