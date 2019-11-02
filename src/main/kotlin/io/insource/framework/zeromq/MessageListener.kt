package io.insource.framework.zeromq

import org.zeromq.api.Message

/**
 * Interface implemented by message listeners to handle subscriptions.
 */
interface MessageListener {
  /**
   * Handle a message payload.
   *
   * @param message The message payload
   */
  fun onMessage(message: Message)
}