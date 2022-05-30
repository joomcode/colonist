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

import com.joom.colonist.processor.descriptors.MethodDescriptor
import com.joom.grip.mirrors.internalName
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes

fun ClassVisitor.newMethod(access: Int, method: MethodDescriptor, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(this, access, method).apply {
    visitCode()
    body()
    returnValue()
    endMethod()
  }
}

inline fun <reified T : Throwable> ClassVisitor.newMethodTryCatch(
  access: Int,
  method: MethodDescriptor,
  noinline tryBody: GeneratorAdapter.() -> Unit,
  noinline catch: GeneratorAdapter.() -> Unit,
) {
  newMethodTryCatch(access, method, T::class.java, tryBody, catch)
}

fun <T : Throwable> ClassVisitor.newMethodTryCatch(
  access: Int,
  method: MethodDescriptor,
  catchThrowableType: Class<out T>,
  body: GeneratorAdapter.() -> Unit,
  catch: GeneratorAdapter.() -> Unit,
) {
  GeneratorAdapter(this, access, method).apply {
    val bodyStart = Label()
    val bodyEnd = Label()
    val handlerStart = Label()
    val handlerEnd = Label()
    visitCode()
    visitTryCatchBlock(bodyStart, bodyEnd, handlerStart, catchThrowableType.internalName)
    visitLabel(bodyStart)
    body()
    visitLabel(bodyEnd)
    visitJumpInsn(Opcodes.GOTO, handlerEnd)

    visitLabel(handlerStart)
    catch()
    visitLabel(handlerEnd)
    returnValue()
    endMethod()
  }
}
