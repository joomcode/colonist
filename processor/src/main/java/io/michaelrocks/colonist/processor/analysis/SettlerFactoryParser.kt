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
import io.michaelrocks.colonist.processor.model.SettlerFactory
import io.michaelrocks.grip.mirrors.Type

interface SettlerFactoryParser {
  fun parseSettlerFactory(settlerFactoryType: Type.Object): SettlerFactory
}

object SettlerFactoryParserImpl : SettlerFactoryParser {
  override fun parseSettlerFactory(settlerFactoryType: Type.Object): SettlerFactory {
    return when (settlerFactoryType) {
      Types.CONSTRUCTOR_SETTLER_FACTORY -> SettlerFactory.Constructor
      Types.CALLBACK_SETTLER_FACTORY -> SettlerFactory.Callback
      Types.CLASS_SETTLER_FACTORY -> SettlerFactory.Class
      else -> SettlerFactory.External(settlerFactoryType)
    }
  }
}
