/*
 * Copyright 2022 SIA Joom
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
import com.joom.colonist.processor.commons.StandaloneClassWriter
import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.commons.contains
import com.joom.colonist.processor.commons.exhaustive
import com.joom.colonist.processor.commons.invokeMethod
import com.joom.colonist.processor.commons.newMethod
import com.joom.colonist.processor.commons.toMethodDescriptor
import com.joom.colonist.processor.descriptors.MethodDescriptor
import com.joom.colonist.processor.model.Colony
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerAcceptor
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.colonist.processor.watermark.WatermarkClassVisitor
import com.joom.grip.ClassRegistry
import com.joom.grip.mirrors.Type
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class ColonyDelegateGenerator(private val classRegistry: ClassRegistry) {

  fun generate(colony: Colony, settlers: Collection<Settler>): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
      Opcodes.V1_6,
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER,
      colony.delegate.internalName,
      null,
      Types.OBJECT_TYPE.internalName,
      null
    )

    generateColonyFoundMethod(classVisitor, colony, settlers)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateColonyFoundMethod(classVisitor: ClassVisitor, colony: Colony, settlers: Collection<Settler>) {
    classVisitor.newMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, MethodDescriptor.forMethod("found", Type.Primitive.Void, colony.type)) {
      for (settler in settlers) {
        produceSettler(colony, settler)
        acceptSettler(colony, settler)
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

    if (!isCallbackStatic) {
      loadArg(0)
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
      loadArg(0)
      swap(settler.type, colony.type)
      invokeMethod(colony.type, callback)
    }
  }
}
