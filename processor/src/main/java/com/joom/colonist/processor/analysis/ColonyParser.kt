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

import com.joom.colonist.processor.ErrorReporter
import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.model.Colony
import com.joom.colonist.processor.model.ColonyMarker
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerAcceptor
import com.joom.colonist.processor.model.SettlerProducer
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type

interface ColonyParser {
  fun parseColony(colonyType: Type.Object, colonyMarker: ColonyMarker, settlers: Collection<Settler>): Colony
}

class ColonyParserImpl(
  private val grip: Grip,
  private val errorReporter: ErrorReporter
) : ColonyParser {

  override fun parseColony(colonyType: Type.Object, colonyMarker: ColonyMarker, settlers: Collection<Settler>): Colony {
    val mirror = grip.classRegistry.getClassMirror(colonyType)
    val settlerProducer = findColonyCallbackMethod(mirror, Types.ON_PRODUCE_SETTLER_TYPE, colonyMarker.type)
    val settlerAcceptor = findColonyCallbackMethod(mirror, Types.ON_ACCEPT_SETTLER_TYPE, colonyMarker.type)

    validateSettlerProducer(colonyType, colonyMarker, settlers, settlerProducer)
    validateSettlerAcceptor(colonyType, colonyMarker, settlers, settlerAcceptor)

    return Colony(colonyType, colonyMarker, settlers, settlerProducer, settlerAcceptor)
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

  private fun validateSettlerProducer(
    colonyType: Type.Object,
    colonyMarker: ColonyMarker,
    settlers: Collection<Settler>,
    settlerProducer: MethodMirror?
  ) {
    if (settlerProducer != null) {
      return
    }

    val settlerTypesWithCallbackProducer = settlers.mapNotNull { settler ->
      val producer = settler.overriddenSettlerProducer ?: colonyMarker.settlerProducer
      settler.type.takeIf { producer is SettlerProducer.Callback }
    }

    if (settlerTypesWithCallbackProducer.isEmpty()) {
      return
    }

    val colonyClassName = colonyType.className
    val settlerClassNames = settlerTypesWithCallbackProducer.joinToString { it.className }
    errorReporter.reportError("Colony $colonyClassName expected to have a producer callback for settlers [$settlerClassNames]")
  }

  private fun validateSettlerAcceptor(
    colonyType: Type.Object,
    colonyMarker: ColonyMarker,
    settlers: Collection<Settler>,
    settlerAcceptor: MethodMirror?
  ) {
    if (settlerAcceptor != null) {
      return
    }

    val settlerTypesWithCallbackAcceptor = settlers.mapNotNull { settler ->
      val acceptor = settler.overriddenSettlerAcceptor ?: colonyMarker.settlerAcceptor
      settler.type.takeIf { acceptor is SettlerAcceptor.Callback }
    }

    if (settlerTypesWithCallbackAcceptor.isEmpty()) {
      return
    }

    val colonyClassName = colonyType.className
    val settlerClassNames = settlerTypesWithCallbackAcceptor.joinToString { it.className }
    errorReporter.reportError("Colony $colonyClassName expected to have an acceptor callback for settlers [$settlerClassNames]")
  }
}
