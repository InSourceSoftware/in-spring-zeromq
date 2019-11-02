package io.insource.framework.zeromq

import org.zeromq.ContextFactory
import org.zeromq.api.Context
import org.zeromq.api.SocketType

/**
 * Factory for creating channels using socket-per-thread semantics.
 */
class ChannelFactory private constructor(
  private val context: Context,
  private val topic: String
) {
  /** ThreadLocal to track objects per thread. */
  private val threadLocal = ThreadLocal.withInitial {
    Channel(context.buildSocket(SocketType.PUSH).connect("inproc://$topic"), topic)
  }

  /**
   * Get or create a channel.
   *
   * @return A thread-local instance of a channel
   */
  fun channel(): Channel {
    return threadLocal.get()
  }

  companion object {
    /**
     * Create a channel factory.
     *
     * @param topic The topic name used to uniquely identify a channel
     * @param context The 0MQ context
     */
    fun create(topic: String = "default", context: Context = ContextFactory.context()): ChannelFactory {
      return ChannelFactory(context, topic)
    }
  }
}