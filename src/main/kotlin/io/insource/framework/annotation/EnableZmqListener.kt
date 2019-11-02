package io.insource.framework.annotation

/**
 * Annotation to enable configuration for a 0MQ listener.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FILE,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
annotation class EnableZmqListener