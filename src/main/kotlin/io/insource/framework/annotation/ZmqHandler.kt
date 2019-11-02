package io.insource.framework.annotation

/**
 * Annotation that marks a method to be the target of a 0MQ message listener
 * within a class that is annotated with `ZmqSubscriber` or `ZmqListener`.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ZmqHandler