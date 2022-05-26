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
import com.joom.colonist.processor.model.SettlerSelector
import com.joom.grip.mirrors.AnnotationMirror

interface SettlerSelectorParser {
  fun parseSettlerSelector(settlerSelectorAnnotation: AnnotationMirror): SettlerSelector
}

object SettlerSelectorParserImpl : SettlerSelectorParser {
  override fun parseSettlerSelector(settlerSelectorAnnotation: AnnotationMirror): SettlerSelector {
    return when (settlerSelectorAnnotation.type) {
      Types.SELECT_SETTLERS_BY_ANNOTATION_TYPE -> {
        SettlerSelector.Annotation(settlerSelectorAnnotation.requireTypeObjectValue("value"))
      }

      Types.SELECT_SETTLERS_BY_SUPER_TYPE_TYPE -> {
        SettlerSelector.SuperType(settlerSelectorAnnotation.requireTypeObjectValue("value"))
      }

      Types.SELECT_SETTLERS_WITH_REGISTERED_SELECTOR_TYPE -> {
        SettlerSelector.Registered
      }

      else -> {
        error("Unsupported settler selector annotation @${settlerSelectorAnnotation.type.className}")
      }
    }
  }
}
