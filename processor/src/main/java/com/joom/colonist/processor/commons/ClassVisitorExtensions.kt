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

import com.joom.colonist.processor.descriptors.MethodDescriptor
import org.objectweb.asm.ClassVisitor

fun ClassVisitor.newMethod(access: Int, method: MethodDescriptor, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(this, access, method).apply {
    visitCode()
    body()
    returnValue()
    endMethod()
  }
}
