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

import io.michaelrocks.colonist.processor.model.ColonyMarker
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.Type

interface ColonyMarkerParser {
  fun parseColonyMarker(colonyAnnotationType: Type.Object): ColonyMarker
}

class ColonyMarkerParserImpl(
  private val grip: Grip,
  private val settlerSelectorParser: SettlerSelectorParser,
  private val settlerProducerParser: SettlerProducerParser,
  private val settlerAcceptorParser: SettlerAcceptorParser
) : ColonyMarkerParser {

  override fun parseColonyMarker(colonyAnnotationType: Type.Object): ColonyMarker {
    val mirror = grip.classRegistry.getClassMirror(colonyAnnotationType)
    val settlerSelector = mirror.getSettlerSelector(settlerSelectorParser)
    val settlerProducer = mirror.getSettlerProducer(settlerProducerParser)
    val settlerAcceptor = mirror.getSettlerAcceptor(settlerAcceptorParser)
    return ColonyMarker(colonyAnnotationType, settlerSelector, settlerProducer, settlerAcceptor)
  }
}
