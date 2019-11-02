package io.insource.framework.annotation

import io.insource.framework.condition.OnEnableZmqListenerCondition
import org.springframework.context.annotation.Conditional

/**
 * Annotation to enable configuration for a 0MQ subscriber when the annotation
 * `@EnableZmqListener` is detected.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FILE,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Conditional(OnEnableZmqListenerCondition::class)
annotation class ConditionalOnEnableZmqListener