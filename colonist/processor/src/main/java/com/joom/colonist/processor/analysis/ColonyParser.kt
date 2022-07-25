/*
 * Copyright 2019 SIA Joom
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
import com.joom.colonist.processor.commons.toMethodDescriptor
import com.joom.colonist.processor.descriptors.MethodDescriptor
import com.joom.colonist.processor.model.Colony
import com.joom.colonist.processor.model.ColonyMarker
import com.joom.colonist.processor.model.SettlerAcceptor
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.grip.Grip
import com.joom.grip.mirrors.ClassMirror
import com.joom.grip.mirrors.MethodMirror
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.getObjectType
import com.joom.grip.mirrors.isPublic

interface ColonyParser {
  fun parseColony(colonyType: Type.Object, colonyMarker: ColonyMarker): Colony
}

class ColonyParserImpl(
  private val grip: Grip,
) : ColonyParser {

  override fun parseColony(colonyType: Type.Object, colonyMarker: ColonyMarker): Colony {
    val mirror = grip.classRegistry.getClassMirror(colonyType)
    val delegate = getObjectType("L__colonist__${colonyType.sanitizedInternalName}_${colonyMarker.type.sanitizedInternalName}_Delegate;")
    val settlerProducer = findColonyCallbackMethod(mirror, Types.ON_PRODUCE_SETTLER_TYPE, colonyMarker.type, Types.CLASS_TYPE)
    val settlerAcceptor = findColonyCallbackMethod(mirror, Types.ON_ACCEPT_SETTLER_TYPE, colonyMarker.type, computeAcceptParameterType(colonyMarker))

    return Colony(colonyType, delegate, colonyMarker, settlerProducer, settlerAcceptor)
  }

  private fun findColonyCallbackMethod(
    mirror: ClassMirror,
    callbackAnnotationType: Type.Object,
    colonyAnnotationType: Type.Object,
    requiredParameterType: Type.Object?,
  ): Colony.CallbackMethod? {
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

    if (requiredParameterType != null) {
      require(method.parameters.first().type == requiredParameterType) {
        "Callback method ${method.name} in class ${mirror.type.className} must have a single argument of type ${requiredParameterType.className}"
      }
    }

    return if (method.isPublic) {
      Colony.CallbackMethod.Direct(method)
    } else {
      Colony.CallbackMethod.Bridged(computeBridgeMethodDescriptor(method), method)
    }
  }

  private fun computeBridgeMethodDescriptor(method: MethodMirror): MethodDescriptor {
    return method.toMethodDescriptor().copy(name = "__bridge__" + method.name)
  }

  private fun computeAcceptParameterType(marker: ColonyMarker): Type.Object? {
    return if (marker.settlerAcceptor is SettlerAcceptor.Callback && marker.settlerProducer is SettlerProducer.Class) {
      Types.CLASS_TYPE
    } else {
      null
    }
  }

  private val Type.sanitizedInternalName: String
    get() = internalName.replace('/', '_')
}
