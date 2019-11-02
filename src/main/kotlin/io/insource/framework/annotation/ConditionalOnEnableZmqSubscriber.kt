package io.insource.framework.annotation

import io.insource.framework.condition.OnEnableZmqSubscriberCondition
import org.springframework.context.annotation.Conditional

/**
 * Annotation to enable configuration for a 0MQ subscriber when the annotation
 * `@EnableZmqSubscriber` is detected.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FILE,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Conditional(OnEnableZmqSubscriberCondition::class)
annotation class ConditionalOnEnableZmqSubscriber