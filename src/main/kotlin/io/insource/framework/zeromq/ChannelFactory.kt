package io.insource.framework.zeromq

import org.zeromq.ContextFactory
import org.zeromq.api.Context
import org.zeromq.api.SocketType

/**
 * Factory for creating channels.
 */
class ChannelFactory private constructor(private val context: Context) {
  /**
   * Create a channel for the given topic.
   *
   * @param topic The topic name used to uniquely identify a channel
   * @return A new Channel
   */
  fun createChannel(topic: String): Channel {
    return Channel(context.buildSocket(SocketType.PUSH).connect("inproc://$topic"), topic)
  }

  companion object {
    /**
     * Create a channel factory.
     *
     * @param context The 0MQ context
     */
    fun create(context: Context = ContextFactory.context()): ChannelFactory {
      return ChannelFactory(context)
    }
  }
}