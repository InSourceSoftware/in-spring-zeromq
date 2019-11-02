package io.insource.framework.condition

import io.insource.framework.annotation.EnableZmqListener

internal class OnEnableZmqListenerCondition : EnableAnnotationCondition<EnableZmqListener>(EnableZmqListener::class) {
  override val prefix: String
    get() = "zmq.listener"
}