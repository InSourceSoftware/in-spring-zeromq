package io.insource.framework.util


import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Utility methods for working with Kotlin Annotations.
 */
object AnnotationUtils {
  /**
   * Get a single-valued annotation instance from a class by type.
   *
   * @param objectClass Any class with class-level annotations
   * @param annotationClass The annotation type to search for
   * @return The annotation instance
   * @throws IllegalArgumentException
   */
  fun <T : Annotation> getAnnotationByType(objectClass: KClass<*>, annotationClass: KClass<T>): T {
    return AnnotatedElementUtils.getMergedAnnotation(objectClass.java, annotationClass.java)
      ?: throw IllegalArgumentException("Annotation not found: ${annotationClass.simpleName}")
  }

  /**
   * Determine if an annotation is available on a class.
   *
   * @return True if the annotation is available on the given class, false otherwise
   */
  fun <T : Annotation> hasAnnotation(objectClass: KClass<*>, annotationClass: KClass<T>): Boolean {
    return AnnotatedElementUtils.hasAnnotation(objectClass.java, annotationClass.java)
  }

  /**
   * Get an annotation from the supplied method.
   *
   * @param method The method to look for annotations on
   * @param annotationType The annotation type to look for
   * @return The first matching annotation, or null if not found
   */
  fun <T : Annotation> getAnnotation(method: Method, annotationType: KClass<T>): T? {
    return AnnotationUtils.getAnnotation(method, annotationType.java)
  }
}