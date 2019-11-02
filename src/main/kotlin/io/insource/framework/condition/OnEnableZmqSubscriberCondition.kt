package io.insource.framework.condition

import io.insource.framework.annotation.EnableZmqSubscriber

internal class OnEnableZmqSubscriberCondition : EnableAnnotationCondition<EnableZmqSubscriber>(EnableZmqSubscriber::class) {
  override val prefix: String
    get() = "zmq.subscriber"
}