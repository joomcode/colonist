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

package com.joom.colonist.processor.commons

import com.joom.colonist.AcceptSettlersAndForget
import com.joom.colonist.AcceptSettlersViaCallback
import com.joom.colonist.Colony
import com.joom.colonist.OnAcceptSettler
import com.joom.colonist.OnProduceSettler
import com.joom.colonist.ProduceSettlersAsClasses
import com.joom.colonist.ProduceSettlersViaCallback
import com.joom.colonist.ProduceSettlersViaConstructor
import com.joom.colonist.SelectSettlersByAnnotation
import com.joom.colonist.SelectSettlersBySuperType
import com.joom.colonist.SelectSettlersWithRegisteredSelector
import com.joom.colonist.internal.ColonyFounder
import com.joom.grip.mirrors.getObjectType

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
