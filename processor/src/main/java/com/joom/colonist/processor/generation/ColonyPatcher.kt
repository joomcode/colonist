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

package com.joom.colonist.processor.generation

import com.joom.colonist.processor.commons.GeneratorAdapter
import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.commons.contains
import com.joom.colonist.processor.commons.exhaustive
import com.joom.colonist.processor.commons.invokeMethod
import com.joom.colonist.processor.commons.newMethod
import com.joom.colonist.processor.commons.toMethodDescriptor
import com.joom.colonist.processor.descriptors.MethodDescriptor
import com.joom.colonist.processor.descriptors.descriptor
import com.joom.colonist.processor.model.Colony
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerAcceptor
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.colonist.processor.watermark.WatermarkClassVisitor
import io.michaelrocks.grip.mirrors.Type
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ColonyPatcher(
  private val classVisitor: ClassVisitor,
  private val colonies: Collection<Colony>
) : WatermarkClassVisitor(classVisitor, false) {

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<String>?) {
    val newInterfaces = combineInterfaces(interfaces, Types.COLONY_FOUNDER_TYPE.internalName)
    super.visit(version, access, name, signature, superName, newInterfaces)
  }

  override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
    if (name == FOUND_METHOD.name && descriptor == FOUND_METHOD.descriptor) {
      return null
    }

    return super.visitMethod(access, name, descriptor, signature, exceptions)
  }

  override fun visitEnd() {
    val methods = composeColonyMethods(colonies)
    generateColonyFounderMethods(methods)

    super.visitEnd()
  }

  private fun generateColonyFounderMethods(methods: Collection<ColonyMethod>) {
    generateColonyFounderDispatcherMethod(methods)
    for (method in methods) {
      generateColonyFounderMethod(method)
    }
  }

  private fun generateColonyFounderDispatcherMethod(methods: Collection<ColonyMethod>) {
    classVisitor.newMethod(Opcodes.ACC_PUBLIC, FOUND_METHOD) {
      loadArg(0)
      ifNull {
        for (method in methods) {
          loadThis()
          invokePrivate(method.colony.type, method.method)
        }
        returnValue()
      }

      for (method in methods) {
        loadArg(0)
        push(method.colony.marker.type)
        ifCmp(Types.CLASS_TYPE, GeneratorAdapter.NE) {
          loadThis()
          invokePrivate(method.colony.type, method.method)
          returnValue()
        }
      }
    }
  }

  private fun generateColonyFounderMethod(method: ColonyMethod) {
    classVisitor.newMethod(Opcodes.ACC_PRIVATE, method.method) {
      for (settler in method.colony.settlers) {
        produceSettler(method.colony, settler)
        acceptSettler(method.colony, settler)
      }
    }
  }

  private fun GeneratorAdapter.produceSettler(colony: Colony, settler: Settler) {
    exhaustive(
      when (settler.overriddenSettlerProducer ?: colony.marker.settlerProducer) {
        SettlerProducer.Constructor -> invokeDefaultConstructor(settler.type)
        SettlerProducer.Callback -> invokeProduceCallback(colony, settler)
        SettlerProducer.Class -> push(settler.type)
      }
    )
  }

  private fun GeneratorAdapter.invokeDefaultConstructor(type: Type.Object) {
    newInstance(type)
    dup()
    invokeConstructor(type, MethodDescriptor.forDefaultConstructor())
  }

  private fun GeneratorAdapter.invokeProduceCallback(colony: Colony, settler: Settler) {
    val callback = colony.settlerProducer!!
    val isCallbackStatic = Opcodes.ACC_STATIC in callback.access

    if (isCallbackStatic) {
      loadThis()
    }

    push(settler.type)

    if (isCallbackStatic) {
      invokeStatic(colony.type, callback.toMethodDescriptor())
    } else {
      invokeMethod(colony.type, callback)
    }
  }

  private fun GeneratorAdapter.acceptSettler(colony: Colony, settler: Settler) {
    exhaustive(
      when (settler.overriddenSettlerAcceptor ?: colony.marker.settlerAcceptor) {
        SettlerAcceptor.None -> pop()
        SettlerAcceptor.Callback -> invokeAcceptCallback(colony, settler)
      }
    )
  }

  private fun GeneratorAdapter.invokeAcceptCallback(colony: Colony, settler: Settler) {
    val callback = colony.settlerAcceptor!!
    val isCallbackStatic = Opcodes.ACC_STATIC in callback.access

    checkCast(callback.parameters[0].type)

    if (isCallbackStatic) {
      invokeStatic(colony.type, callback.toMethodDescriptor())
    } else {
      loadThis()
      swap(settler.type, colony.type)
      invokeMethod(colony.type, callback)
    }
  }

  private class ColonyMethod(
    val colony: Colony,
    val method: MethodDescriptor
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
      val name = "${FOUND_METHOD.name}__colonist__${colony.marker.type.internalName.replace('/', '_')}"
      val method = MethodDescriptor.forMethod(name, Type.Primitive.Void)
      return ColonyMethod(colony, method)
    }
  }
}
