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

package com.joom.colonist.processor.commons

import com.joom.colonist.processor.analysis.optionalValue
import com.joom.colonist.processor.analysis.requireValue
import com.joom.colonist.processor.descriptors.FieldDescriptor
import com.joom.colonist.processor.descriptors.MethodDescriptor
import com.joom.grip.mirrors.ClassMirror
import com.joom.grip.mirrors.FieldMirror
import com.joom.grip.mirrors.MethodMirror
import kotlinx.metadata.Flag
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata

fun MethodMirror.toMethodDescriptor(): MethodDescriptor {
  return MethodDescriptor(name, type)
}

fun FieldMirror.toFieldDescriptor(): FieldDescriptor {
  return FieldDescriptor(name, type)
}

fun ClassMirror.isKotlinObject(): Boolean {
  val metadata = annotations[Types.KOTLIN_METADATA_TYPE] ?: return false

  val header = KotlinClassHeader(
    kind = metadata.optionalValue("k"),
    metadataVersion = metadata.requireValue("mv"),
    data1 = metadata.optionalValue<List<String>>("d1")?.toTypedArray(),
    data2 = metadata.optionalValue<List<String>>("d2")?.toTypedArray(),
    extraString = metadata.optionalValue("xs"),
    packageName = metadata.optionalValue("pn"),
    extraInt = metadata.optionalValue("xi"),
  )

  val classMetadata = KotlinClassMetadata.read(header)

  return if (classMetadata is KotlinClassMetadata.Class) {
    Flag.Class.IS_OBJECT(classMetadata.toKmClass().flags)
  } else {
    false
  }
}
