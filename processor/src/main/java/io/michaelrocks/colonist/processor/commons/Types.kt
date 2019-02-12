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
import io.michaelrocks.colonist.CallbackSettlerFactory
import io.michaelrocks.colonist.ClassSettlerFactory
import io.michaelrocks.colonist.Colony
import io.michaelrocks.colonist.ConstructorSettlerFactory
import io.michaelrocks.colonist.NoneSettlerAcceptor
import io.michaelrocks.colonist.OnAcceptSettler
import io.michaelrocks.colonist.OnCreateSettler
import io.michaelrocks.colonist.internal.ColonyFounder
import io.michaelrocks.grip.mirrors.getObjectType
import java.lang.reflect.Type as JavaType

object Types {
  val OBJECT_TYPE = getObjectType<Any>()
  val CLASS_TYPE = getObjectType<Class<*>>()

  val COLONY_TYPE = getObjectType<Colony>()
  val ON_CREATE_SETTLER_TYPE = getObjectType<OnCreateSettler>()
  val ON_ACCEPT_SETTLER_TYPE = getObjectType<OnAcceptSettler>()
  val CONSTRUCTOR_SETTLER_FACTORY = getObjectType<ConstructorSettlerFactory<*>>()
  val CALLBACK_SETTLER_FACTORY = getObjectType<CallbackSettlerFactory<*>>()
  val CLASS_SETTLER_FACTORY = getObjectType<ClassSettlerFactory>()
  val NONE_SETTLER_ACCEPTOR = getObjectType<NoneSettlerAcceptor<*, *>>()
  val CALLBACK_SETTLER_ACCEPTOR = getObjectType<CallbackSettlerAcceptor<*, *>>()

  val COLONY_FOUNDER_TYPE = getObjectType<ColonyFounder>()
}
