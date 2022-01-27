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

package com.joom.colonist.processor.watermark

import org.objectweb.asm.Attribute
import org.objectweb.asm.ByteVector
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label

class ColonistAttribute private constructor(type: String) : Attribute(type) {
  constructor() : this("Colonist")

  override fun read(
    classReader: ClassReader,
    offset: Int,
    length: Int,
    buffer: CharArray?,
    codeOffset: Int,
    labels: Array<Label>?
  ): ColonistAttribute {
    return ColonistAttribute()
  }

  override fun write(
    classWriter: ClassWriter?,
    code: ByteArray?,
    length: Int,
    maxStack: Int,
    maxLocals: Int
  ): ByteVector {
    return ByteVector()
  }
}
