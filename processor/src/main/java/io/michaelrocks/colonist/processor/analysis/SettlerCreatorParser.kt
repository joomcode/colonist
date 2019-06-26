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
import io.michaelrocks.colonist.processor.model.SettlerProducer
import io.michaelrocks.grip.mirrors.Type

interface SettlerProducerParser {
  fun parseSettlerProducer(settlerProducerType: Type.Object): SettlerProducer
}

object SettlerProducerParserImpl : SettlerProducerParser {
  override fun parseSettlerProducer(settlerProducerType: Type.Object): SettlerProducer {
    return when (settlerProducerType) {
      Types.CONSTRUCTOR_SETTLER_PRODUCER -> SettlerProducer.Constructor
      Types.CALLBACK_SETTLER_PRODUCER -> SettlerProducer.Callback
      Types.CLASS_SETTLER_PRODUCER -> SettlerProducer.Class
      else -> SettlerProducer.External(settlerProducerType)
    }
  }
}
