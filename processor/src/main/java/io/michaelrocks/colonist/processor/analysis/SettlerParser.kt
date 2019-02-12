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
import io.michaelrocks.colonist.processor.model.Settler
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.Type

interface SettlerParser {
  fun parseSettler(settlerType: Type.Object, colonyMarker: ColonyMarker): Settler
}

class SettlerParserImpl(
  private val grip: Grip,
  private val settlerFactoryParser: SettlerFactoryParser,
  private val settlerAcceptorParser: SettlerAcceptorParser
) : SettlerParser {

  override fun parseSettler(settlerType: Type.Object, colonyMarker: ColonyMarker): Settler {
    val mirror = grip.classRegistry.getClassMirror(settlerType)
    val annotation = requireNotNull(mirror.annotations[colonyMarker.settlerMarker.type]) {
      "${settlerType.className} must be annotated with @${colonyMarker.settlerMarker.type.className} annotation"
    }

    val settlerFactory = colonyMarker.settlerMarker.settlerFactoryPropertyName?.let { settlerFactoryPropertyName ->
      val settlerFactoryType = annotation.requireValue<Type.Object>(settlerFactoryPropertyName)
      settlerFactoryParser.parseSettlerFactory(settlerFactoryType)
    } ?: colonyMarker.defaultSettlerFactory
    val settlerAcceptor = colonyMarker.settlerMarker.settlerAcceptorPropertyName?.let { settlerAcceptorPropertyName ->
      val settlerAcceptorType = annotation.requireValue<Type.Object>(settlerAcceptorPropertyName)
      settlerAcceptorParser.parseSettlerAcceptor(settlerAcceptorType)
    } ?: colonyMarker.defaultSettlerAcceptor

    return Settler(settlerType, settlerFactory, settlerAcceptor)
  }
}
