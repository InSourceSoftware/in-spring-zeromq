package io.insource.framework.zeromq

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ContextFactory
import org.zeromq.api.Context
import org.zeromq.api.SocketType

/**
 * Component that will proxy streams of messages to a remote server.
 */
class ChannelProxy private constructor(
  private val context: Context,
  private val topics: List<String>
) {
  /**
   * Create a 0MQ device that will proxy streams of events or messages from
   * threads inside this JVM to a remote server.
   *
   * @param host The host name to connect to
   * @param port The port to connect to
   */
  fun connect(host: String = "localhost", port: Int = 1337): ChannelProxy {
    for (topic in topics) {
      LOGGER.info("Forwarding messages on topic $topic to tcp://$host:$port")

      val frontend = context.buildSocket(SocketType.PULL).bind("inproc://$topic")
      val backend = context.buildSocket(SocketType.PUB).connect("tcp://$host:$port")
      context.forward(frontend, backend)
    }

    return this
  }

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(ChannelProxy::class.java)

    /**
     * Create a new Channels.
     *
     * @param topics A list of topic names to bind to
     * @param context The 0MQ context
     */
    fun create(topics: List<String>, context: Context = ContextFactory.context()): ChannelProxy {
      return ChannelProxy(context, topics)
    }
  }
}