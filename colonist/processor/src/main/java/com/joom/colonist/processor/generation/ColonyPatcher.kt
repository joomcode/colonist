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

package com.joom.colonist.processor.generation

import com.joom.colonist.processor.commons.GeneratorAdapter
import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.commons.invokeMethod
import com.joom.colonist.processor.commons.newMethod
import com.joom.colonist.processor.commons.newMethodTryCatch
import com.joom.colonist.processor.commons.toMethodDescriptor
import com.joom.colonist.processor.descriptors.MethodDescriptor
import com.joom.colonist.processor.descriptors.descriptor
import com.joom.colonist.processor.model.Colony
import com.joom.colonist.processor.watermark.WatermarkClassVisitor
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.isStatic
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PROTECTED
import org.objectweb.asm.Opcodes.ACC_PUBLIC

class ColonyPatcher(
  private val classVisitor: ClassVisitor,
  private val colonies: Collection<Colony>
) : WatermarkClassVisitor(classVisitor, false) {

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<String>?
  ) {
    val newInterfaces = combineInterfaces(interfaces, Types.COLONY_FOUNDER_TYPE.internalName)
    super.visit(version, makeAccessPublic(access), name, signature, superName, newInterfaces)
  }

  override fun visitMethod(
    access: Int,
    name: String,
    descriptor: String,
    signature: String?,
    exceptions: Array<out String>?
  ): MethodVisitor? {
    if (name == FOUND_METHOD.name && descriptor == FOUND_METHOD.descriptor) {
      return null
    }

    return super.visitMethod(access, name, descriptor, signature, exceptions)
  }

  override fun visitEnd() {
    val methods = composeColonyMethods(colonies)
    generateColonyFounderMethods(methods)
    colonies.forEach { colony ->
      if (colony.settlerAcceptor is Colony.CallbackMethod.Bridged) {
        generateBridgeMethod(colony.type, colony.settlerAcceptor)
      }

      if (colony.settlerProducer is Colony.CallbackMethod.Bridged) {
        generateBridgeMethod(colony.type, colony.settlerProducer)
      }
    }

    super.visitEnd()
  }

  private fun makeAccessPublic(access: Int): Int {
    return (access and (ACC_PRIVATE or ACC_PROTECTED).inv()) or ACC_PUBLIC
  }

  private fun generateColonyFounderMethods(methods: Collection<ColonyMethod>) {
    generateColonyFounderDispatcherMethod(methods)
  }

  private fun generateColonyFounderDispatcherMethod(methods: Collection<ColonyMethod>) {
    classVisitor.newMethodTryCatch<NoClassDefFoundError>(Opcodes.ACC_PUBLIC, FOUND_METHOD, tryBody = {
      loadArg(0)
      ifNull {
        for (method in methods) {
          loadThis()
          invokeStatic(method.colony.delegate, method.delegateMethod)
        }
        returnValue()
      }

      for (method in methods) {
        loadArg(0)
        push(method.colony.marker.type)
        ifCmp(Types.CLASS_TYPE, GeneratorAdapter.EQ) {
          loadThis()
          invokeStatic(method.colony.delegate, method.delegateMethod)
          returnValue()
        }
      }
    }, catch = {
      throwException(Types.COLONIST_EXCEPTION_TYPE, "Failed to find colony delegate, is colonist plugin applied to application module?")
    })
  }

  private fun generateBridgeMethod(type: Type, bridgedMethod: Colony.CallbackMethod.Bridged) {
    classVisitor.newMethod(makeAccessPublic(bridgedMethod.method.access), bridgedMethod.bridge) {
      if (!bridgedMethod.method.isStatic) {
        loadThis()
      }

      bridgedMethod.method.parameters.forEachIndexed { index, _ ->
        loadArg(index)
      }

      if (!bridgedMethod.method.isStatic) {
        invokeMethod(type, bridgedMethod.method)
      } else {
        invokeStatic(type, bridgedMethod.method.toMethodDescriptor())
      }

      returnValue()
    }
  }

  private class ColonyMethod(
    val colony: Colony,
    val delegateMethod: MethodDescriptor,
  )

  companion object {
    private val FOUND_METHOD = MethodDescriptor.forMethod("found", Type.Primitive.Void, Types.CLASS_TYPE)

    private fun combineInterfaces(interfaces: Array<String>?, newInterface: String): Array<String>? {
      return when {
        interfaces == null -> arrayOf(newInterface)
        newInterface in interfaces -> interfaces
        else -> interfaces + newInterface
      }
    }

    private fun composeColonyMethods(colonies: Collection<Colony>): Collection<ColonyMethod> {
      return colonies.map { composeColonyMethod(it) }
    }

    private fun composeColonyMethod(colony: Colony): ColonyMethod {
      val delegateMethod = MethodDescriptor.forMethod("found", Type.Primitive.Void, colony.type)
      return ColonyMethod(colony, delegateMethod)
    }
  }
}
