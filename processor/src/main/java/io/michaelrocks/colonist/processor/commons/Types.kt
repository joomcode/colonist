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

package io.michaelrocks.colonist.processor.commons

import io.michaelrocks.colonist.AcceptSettlersAndForget
import io.michaelrocks.colonist.AcceptSettlersViaCallback
import io.michaelrocks.colonist.Colony
import io.michaelrocks.colonist.OnAcceptSettler
import io.michaelrocks.colonist.OnProduceSettler
import io.michaelrocks.colonist.ProduceSettlersAsClasses
import io.michaelrocks.colonist.ProduceSettlersViaCallback
import io.michaelrocks.colonist.ProduceSettlersViaConstructor
import io.michaelrocks.colonist.SelectSettlersByAnnotation
import io.michaelrocks.colonist.SelectSettlersBySuperType
import io.michaelrocks.colonist.SelectSettlersWithRegisteredSelector
import io.michaelrocks.colonist.internal.ColonyFounder
import io.michaelrocks.grip.mirrors.getObjectType

object Types {
  val OBJECT_TYPE = getObjectType<Any>()
  val CLASS_TYPE = getObjectType<Class<*>>()

  val COLONY_TYPE = getObjectType<Colony>()
  val COLONY_FOUNDER_TYPE = getObjectType<ColonyFounder>()

  val SELECT_SETTLERS_BY_ANNOTATION_TYPE = getObjectType<SelectSettlersByAnnotation>()
  val SELECT_SETTLERS_BY_SUPER_TYPE_TYPE = getObjectType<SelectSettlersBySuperType>()
  val SELECT_SETTLERS_WITH_REGISTERED_SELECTOR_TYPE = getObjectType<SelectSettlersWithRegisteredSelector>()

  val PRODUCE_SETTLERS_AS_CLASSES_TYPE = getObjectType<ProduceSettlersAsClasses>()
  val PRODUCE_SETTLERS_VIA_CALLBACK_TYPE = getObjectType<ProduceSettlersViaCallback>()
  val PRODUCE_SETTLERS_VIA_CONSTRUCTOR_TYPE = getObjectType<ProduceSettlersViaConstructor>()

  val ACCEPT_SETTLERS_AND_FORGET_TYPE = getObjectType<AcceptSettlersAndForget>()
  val ACCEPT_SETTLERS_VIA_CALLBACK_TYPE = getObjectType<AcceptSettlersViaCallback>()

  val ON_PRODUCE_SETTLER_TYPE = getObjectType<OnProduceSettler>()
  val ON_ACCEPT_SETTLER_TYPE = getObjectType<OnAcceptSettler>()
}
