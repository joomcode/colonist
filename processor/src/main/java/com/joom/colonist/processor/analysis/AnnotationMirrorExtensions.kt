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

package com.joom.colonist.processor.analysis

import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.Type

inline fun <reified T : Any> AnnotationMirror.requireValue(name: String): T {
  val value = requireNotNull(values[name]) { "Cannot read value $name from $this" }
  require(value is T) { "Value $name from $this is of type ${value.javaClass} but expected to have type ${T::class.java}" }
  return value
}

fun AnnotationMirror.requireTypeObjectValue(name: String): Type.Object {
  val value = requireValue<Type>(name)
  return value as? Type.Object ?: error("Value $name from $this is ${value.javaClass.simpleName} but expected be a class")
}
