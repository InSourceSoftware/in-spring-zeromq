package io.insource.framework.condition

import org.springframework.boot.autoconfigure.condition.ConditionMessage
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata
import kotlin.reflect.KClass

internal abstract class EnableAnnotationCondition<T : Annotation>(private val annotationClass: KClass<T>) : SpringBootCondition() {
  private val annotationName = annotationClass.simpleName

  protected abstract val prefix: String

  override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
    val message = ConditionMessage.forCondition("@$annotationName Condition")

    return if (isEnabled(context) || hasEnableAnnotation(context)) {
      ConditionOutcome.match(message.found("@$annotationName annotation").items(annotationName))
    } else {
      ConditionOutcome.noMatch(message.didNotFind("@$annotationName annotation").atAll())
    }
  }

  private fun isEnabled(context: ConditionContext): Boolean {
    return context.environment.getProperty(String.format("%s.%s", prefix, "enabled"), Boolean::class.java, java.lang.Boolean.FALSE)
  }

  private fun hasEnableAnnotation(context: ConditionContext): Boolean {
    return context.beanFactory?.getBeanNamesForAnnotation(annotationClass.java)?.isNotEmpty() ?: false
  }
}
