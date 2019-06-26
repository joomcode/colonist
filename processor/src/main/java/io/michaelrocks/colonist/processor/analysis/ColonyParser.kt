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

package io.michaelrocks.colonist.processor.analysis

import io.michaelrocks.colonist.processor.ErrorReporter
import io.michaelrocks.colonist.processor.commons.Types
import io.michaelrocks.colonist.processor.model.Colony
import io.michaelrocks.colonist.processor.model.ColonyMarker
import io.michaelrocks.colonist.processor.model.Settler
import io.michaelrocks.colonist.processor.model.SettlerAcceptor
import io.michaelrocks.colonist.processor.model.SettlerProducer
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type

interface ColonyParser {
  fun parseColony(colonyType: Type.Object, colonyMarker: ColonyMarker): Colony
}

class ColonyParserImpl(
  private val grip: Grip,
  private val annotationIndex: AnnotationIndex,
  private val settlerParser: SettlerParser,
  private val errorReporter: ErrorReporter
) : ColonyParser {

  override fun parseColony(colonyType: Type.Object, colonyMarker: ColonyMarker): Colony {
    val mirror = grip.classRegistry.getClassMirror(colonyType)
    // TODO: Make settlers parse once when there're multiple colonies with the same marker.
    val settlers = parseSettlers(colonyMarker)
    val settlerProducer = findColonyCallbackMethod(mirror, Types.ON_PRODUCE_SETTLER_TYPE, colonyMarker.type)
    val settlerAcceptor = findColonyCallbackMethod(mirror, Types.ON_ACCEPT_SETTLER_TYPE, colonyMarker.type)

    validateSettlerProducer(colonyType, settlers, settlerProducer)
    validateSettlerAcceptor(colonyType, settlers, settlerAcceptor)

    return Colony(colonyType, colonyMarker, settlers, settlerProducer, settlerAcceptor)
  }

  private fun parseSettlers(colonyMarker: ColonyMarker): Collection<Settler> {
    val settlerTypes = annotationIndex.findClassesWithAnnotation(colonyMarker.settlerMarker.type)
    return settlerTypes.map { settlerParser.parseSettler(it, colonyMarker) }
  }

  private fun findColonyCallbackMethod(mirror: ClassMirror, callbackAnnotationType: Type.Object, colonyAnnotationType: Type.Object): MethodMirror? {
    val methods = mirror.methods.filter { method ->
      method.annotations.any { annotation ->
        annotation.type == callbackAnnotationType && annotation.values["colonyAnnotation"] == colonyAnnotationType
      }
    }

    if (methods.isEmpty()) {
      return null
    }

    require(methods.size == 1) {
      val className = mirror.type.className
      val callbackAnnotationClassName = callbackAnnotationType.className
      val colonyAnnotationClassName = colonyAnnotationType.className
      val methodsString = methods.joinToString(separator = "\n  ", prefix = "\n  ") { it.name }
      "Class $className contains multiple methods annotated with @$callbackAnnotationClassName} for colony @$colonyAnnotationClassName:$methodsString"
    }

    val method = methods[0]
    require(method.parameters.size == 1) {
      "Callback method ${method.name} in class ${mirror.type.className} must have a single argument for a settler"
    }

    return method
  }

  private fun validateSettlerProducer(colonyType: Type.Object, settlers: Collection<Settler>, settlerProducer: MethodMirror?) {
    if (settlerProducer != null) {
      return
    }

    val settlerTypesWithCallbackProducer = settlers.mapNotNull { settler ->
      settler.type.takeIf { settler.settlerProducer is SettlerProducer.Callback }
    }

    if (settlerTypesWithCallbackProducer.isEmpty()) {
      return
    }

    val colonyClassName = colonyType.className
    val settlerClassNames = settlerTypesWithCallbackProducer.joinToString { it.className }
    errorReporter.reportError("Colony $colonyClassName expected to have a producer callback for settlers [$settlerClassNames]")
  }

  private fun validateSettlerAcceptor(colonyType: Type.Object, settlers: Collection<Settler>, settlerAcceptor: MethodMirror?) {
    if (settlerAcceptor != null) {
      return
    }

    val settlerTypesWithCallbackAcceptor = settlers.mapNotNull { settler ->
      settler.type.takeIf { settler.settlerAcceptor is SettlerAcceptor.Callback }
    }

    if (settlerTypesWithCallbackAcceptor.isEmpty()) {
      return
    }

    val colonyClassName = colonyType.className
    val settlerClassNames = settlerTypesWithCallbackAcceptor.joinToString { it.className }
    errorReporter.reportError("Colony $colonyClassName expected to have an acceptor callback for settlers [$settlerClassNames]")
  }
}
