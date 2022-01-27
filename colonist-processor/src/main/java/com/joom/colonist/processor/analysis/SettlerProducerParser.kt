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
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.grip.mirrors.AnnotationMirror

interface SettlerProducerParser {
  fun parseSettlerProducer(settlerProducerAnnotation: AnnotationMirror): SettlerProducer
}

object SettlerProducerParserImpl : SettlerProducerParser {
  override fun parseSettlerProducer(settlerProducerAnnotation: AnnotationMirror): SettlerProducer {
    return when (settlerProducerAnnotation.type) {
      Types.PRODUCE_SETTLERS_AS_CLASSES_TYPE -> SettlerProducer.Class
      Types.PRODUCE_SETTLERS_VIA_CALLBACK_TYPE -> SettlerProducer.Callback
      Types.PRODUCE_SETTLERS_VIA_CONSTRUCTOR_TYPE -> SettlerProducer.Constructor
      else -> error("Unsupported settler producer annotation @${settlerProducerAnnotation.type.className}")
    }
  }
}
