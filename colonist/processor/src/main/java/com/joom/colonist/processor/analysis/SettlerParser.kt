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

import com.joom.colonist.processor.commons.isKotlinObject
import com.joom.colonist.processor.model.Settler
import com.joom.grip.Grip
import com.joom.grip.mirrors.Type

interface SettlerParser {
  fun parseSettler(settlerType: Type.Object): Settler
}

class SettlerParserImpl(
  private val grip: Grip,
  private val settlerProducerParser: SettlerProducerParser,
  private val settlerAcceptorParser: SettlerAcceptorParser
) : SettlerParser {

  override fun parseSettler(settlerType: Type.Object): Settler {
    val mirror = grip.classRegistry.getClassMirror(settlerType)
    val settlerProducer = mirror.getSettlerProducerOrNull(settlerProducerParser)
    val settlerAcceptor = mirror.getSettlerAcceptorOrNull(settlerAcceptorParser)
    val isKotlinObject = mirror.isKotlinObject()
    return Settler(settlerType, isKotlinObject, settlerProducer, settlerAcceptor)
  }
}
