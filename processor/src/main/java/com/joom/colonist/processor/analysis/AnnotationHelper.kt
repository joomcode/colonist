/*
 * Copyright 2019 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joom.colonist.processor.analysis

import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.model.SettlerAcceptor
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.colonist.processor.model.SettlerSelector
import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Type

private val SETTLER_SELECTOR_ANNOTATION_TYPES = listOf(
  Types.SELECT_SETTLERS_BY_ANNOTATION_TYPE,
  Types.SELECT_SETTLERS_BY_SUPER_TYPE_TYPE,
  Types.SELECT_SETTLERS_WITH_REGISTERED_SELECTOR_TYPE
)

private val SETTLER_PRODUCER_ANNOTATION_TYPES = listOf(
  Types.PRODUCE_SETTLERS_AS_CLASSES_TYPE,
  Types.PRODUCE_SETTLERS_VIA_CALLBACK_TYPE,
  Types.PRODUCE_SETTLERS_VIA_CONSTRUCTOR_TYPE
)

private val SETTLER_ACCEPTOR_ANNOTATION_TYPES = listOf(
  Types.ACCEPT_SETTLERS_AND_FORGET_TYPE,
  Types.ACCEPT_SETTLERS_VIA_CALLBACK_TYPE
)

private const val SETTLER_SELECTOR_ANNOTATION_KIND = "selector"
private const val SETTLER_PRODUCER_ANNOTATION_KIND = "producer"
private const val SETTLER_ACCEPROR_ANNOTATION_KIND = "acceptor"

fun ClassMirror.getSettlerSelector(settlerSelectorParser: SettlerSelectorParser): SettlerSelector {
  val annotation = getSingleMatchingAnnotation(
    SETTLER_SELECTOR_ANNOTATION_TYPES,
    SETTLER_SELECTOR_ANNOTATION_KIND
  )
  return settlerSelectorParser.parseSettlerSelector(annotation)
}

fun ClassMirror.getSettlerSelectorOrNull(settlerSelectorParser: SettlerSelectorParser): SettlerSelector? {
  val annotation = getAtMostOneMatchingAnnotation(
    SETTLER_SELECTOR_ANNOTATION_TYPES,
    SETTLER_SELECTOR_ANNOTATION_KIND
  ) ?: return null
  return settlerSelectorParser.parseSettlerSelector(annotation)
}

fun ClassMirror.getSettlerProducer(settlerProducerParser: SettlerProducerParser): SettlerProducer {
  val annotation = getSingleMatchingAnnotation(
    SETTLER_PRODUCER_ANNOTATION_TYPES,
    SETTLER_PRODUCER_ANNOTATION_KIND
  )
  return settlerProducerParser.parseSettlerProducer(annotation)
}

fun ClassMirror.getSettlerProducerOrNull(settlerProducerParser: SettlerProducerParser): SettlerProducer? {
  val annotation = getAtMostOneMatchingAnnotation(
    SETTLER_PRODUCER_ANNOTATION_TYPES,
    SETTLER_PRODUCER_ANNOTATION_KIND
  ) ?: return null
  return settlerProducerParser.parseSettlerProducer(annotation)
}

fun ClassMirror.getSettlerAcceptor(settlerAcceptorParser: SettlerAcceptorParser): SettlerAcceptor {
  val annotation = getSingleMatchingAnnotation(
    SETTLER_ACCEPTOR_ANNOTATION_TYPES,
    SETTLER_ACCEPROR_ANNOTATION_KIND
  )
  return settlerAcceptorParser.parseSettlerAcceptor(annotation)
}

fun ClassMirror.getSettlerAcceptorOrNull(settlerAcceptorParser: SettlerAcceptorParser): SettlerAcceptor? {
  val annotation = getAtMostOneMatchingAnnotation(
    SETTLER_ACCEPTOR_ANNOTATION_TYPES,
    SETTLER_ACCEPROR_ANNOTATION_KIND
  ) ?: return null
  return settlerAcceptorParser.parseSettlerAcceptor(annotation)
}

private fun ClassMirror.getSingleMatchingAnnotation(annotationTypes: Iterable<Type.Object>, annotationKind: String): AnnotationMirror {
  return getSingleMatchingAnnotation(annotationTypes) { annotations ->
    composeErrorMessage(this, annotationKind, annotations)
  }
}

private inline fun ClassMirror.getSingleMatchingAnnotation(
  annotationTypes: Iterable<Type.Object>,
  message: (List<AnnotationMirror>) -> String
): AnnotationMirror {
  val annotations = getMatchingAnnotations(annotationTypes)
  return annotations.singleOrNull() ?: error(message(annotations))
}

private fun ClassMirror.getAtMostOneMatchingAnnotation(annotationTypes: Iterable<Type.Object>, annotationKind: String): AnnotationMirror? {
  return getAtMostOneMatchingAnnotation(annotationTypes) { annotations ->
    composeErrorMessage(this, annotationKind, annotations)
  }
}

private inline fun ClassMirror.getAtMostOneMatchingAnnotation(
  annotationTypes: Iterable<Type.Object>,
  message: (List<AnnotationMirror>) -> String
): AnnotationMirror? {
  val annotations = getMatchingAnnotations(annotationTypes)
  return if (annotations.size <= 1) annotations.firstOrNull() else error(message(annotations))
}

private fun ClassMirror.getMatchingAnnotations(annotationTypes: Iterable<Type.Object>): List<AnnotationMirror> {
  return annotationTypes.mapNotNull { annotations[it] }
}

private fun composeErrorMessage(mirror: ClassMirror, annotationKind: String, annotations: List<AnnotationMirror>): String {
  return if (annotations.isEmpty()) {
    "Class ${mirror.type.className} doesn't have any $annotationKind annotation"
  } else {
    val annotationsString = annotations.joinToString { "@${it.type.className}" }
    "Class ${mirror.type.className} has multiple $annotationKind annotation: $annotationsString"
  }
}
