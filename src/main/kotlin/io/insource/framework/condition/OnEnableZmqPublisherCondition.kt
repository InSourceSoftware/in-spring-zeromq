package io.insource.framework.condition

import io.insource.framework.annotation.EnableZmqPublisher

internal class OnEnableZmqPublisherCondition : EnableAnnotationCondition<EnableZmqPublisher>(EnableZmqPublisher::class) {
  override val prefix: String
    get() = "zmq.publisher"
}