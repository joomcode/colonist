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

import io.michaelrocks.colonist.CallbackSettlerAcceptor
import io.michaelrocks.colonist.CallbackSettlerProducer
import io.michaelrocks.colonist.ClassSettlerProducer
import io.michaelrocks.colonist.Colony
import io.michaelrocks.colonist.ConstructorSettlerProducer
import io.michaelrocks.colonist.NoneSettlerAcceptor
import io.michaelrocks.colonist.OnAcceptSettler
import io.michaelrocks.colonist.OnProduceSettler
import io.michaelrocks.colonist.internal.ColonyFounder
import io.michaelrocks.grip.mirrors.getObjectType

object Types {
  val OBJECT_TYPE = getObjectType<Any>()
  val CLASS_TYPE = getObjectType<Class<*>>()

  val COLONY_TYPE = getObjectType<Colony>()
  val ON_PRODUCE_SETTLER_TYPE = getObjectType<OnProduceSettler>()
  val ON_ACCEPT_SETTLER_TYPE = getObjectType<OnAcceptSettler>()
  val CONSTRUCTOR_SETTLER_PRODUCER = getObjectType<ConstructorSettlerProducer<*>>()
  val CALLBACK_SETTLER_PRODUCER = getObjectType<CallbackSettlerProducer<*>>()
  val CLASS_SETTLER_PRODUCER = getObjectType<ClassSettlerProducer>()
  val NONE_SETTLER_ACCEPTOR = getObjectType<NoneSettlerAcceptor<*, *>>()
  val CALLBACK_SETTLER_ACCEPTOR = getObjectType<CallbackSettlerAcceptor<*, *>>()

  val COLONY_FOUNDER_TYPE = getObjectType<ColonyFounder>()
}
