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

import io.michaelrocks.colonist.processor.commons.Types
import io.michaelrocks.colonist.processor.model.ColonyMarker
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.Type

interface ColonyMarkerParser {
  fun parseColonyMarker(colonyAnnotationType: Type.Object): ColonyMarker
}

class ColonyMarkerParserImpl(
  private val grip: Grip,
  private val settlerMarkerParser: SettlerMarkerParser,
  private val settlerFactoryParser: SettlerFactoryParser,
  private val settlerAcceptorParser: SettlerAcceptorParser
) : ColonyMarkerParser {

  override fun parseColonyMarker(colonyAnnotationType: Type.Object): ColonyMarker {
    val mirror = grip.classRegistry.getClassMirror(colonyAnnotationType)
    val annotation = requireNotNull(mirror.annotations[Types.COLONY_TYPE]) {
      "${colonyAnnotationType.className} must be annotated with @Colony annotation"
    }

    val settlerAnnotationType = annotation.requireValue<Type.Object>("settlerAnnotation")
    val defaultSettlerFactoryType = annotation.requireValue<Type.Object>("defaultSettlerFactory")
    val defaultSettlerAcceptorType = annotation.requireValue<Type.Object>("defaultSettlerAcceptor")

    val settlerMarker = settlerMarkerParser.parseSettlerMarker(settlerAnnotationType)
    val settlerFactory = settlerFactoryParser.parseSettlerFactory(defaultSettlerFactoryType)
    val settlerAcceptor = settlerAcceptorParser.parseSettlerAcceptor(defaultSettlerAcceptorType)
    return ColonyMarker(colonyAnnotationType, settlerMarker, settlerFactory, settlerAcceptor)
  }
}
