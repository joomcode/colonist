/*
 * Copyright 2022 SIA Joom
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
import com.joom.colonist.processor.model.Colony
import com.joom.colonist.processor.model.ColonyMarker
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerAcceptor
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.grip.mirrors.MethodMirror
import com.joom.grip.mirrors.Type

interface ColonyValidator {
  fun validateColony(colony: Colony, settlers: Collection<Settler>)
}

class ColonyValidatorImpl(
  private val errorReporter: ErrorReporter,
) : ColonyValidator {
  override fun validateColony(colony: Colony, settlers: Collection<Settler>) {
    validateSettlerProducer(colony.type, colony.marker, settlers, colony.settlerProducer)
    validateSettlerAcceptor(colony.type, colony.marker, settlers, colony.settlerAcceptor)
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
