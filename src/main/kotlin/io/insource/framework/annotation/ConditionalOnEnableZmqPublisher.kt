package io.insource.framework.annotation

import io.insource.framework.condition.OnEnableZmqPublisherCondition
import org.springframework.context.annotation.Conditional

/**
 * Annotation to enable configuration for a 0MQ publisher when the annotation
 * `@EnableZmqPublisher` is detected.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FILE,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Conditional(OnEnableZmqPublisherCondition::class)
annotation class ConditionalOnEnableZmqPublisher