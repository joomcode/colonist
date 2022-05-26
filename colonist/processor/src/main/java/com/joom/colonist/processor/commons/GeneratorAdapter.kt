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

import com.joom.colonist.processor.descriptors.FieldDescriptor
import com.joom.colonist.processor.descriptors.MethodDescriptor
import com.joom.colonist.processor.descriptors.descriptor
import com.joom.colonist.processor.descriptors.isConstructor
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.isArray
import com.joom.grip.mirrors.toAsmType
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACONST_NULL
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Opcodes.DOUBLE
import org.objectweb.asm.Opcodes.FLOAT
import org.objectweb.asm.Opcodes.INTEGER
import org.objectweb.asm.Opcodes.INVOKEINTERFACE
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.LONG
import org.objectweb.asm.Opcodes.TOP
import org.objectweb.asm.commons.GeneratorAdapter as AsmGeneratorAdapter

class GeneratorAdapter(
  methodVisitor: MethodVisitor,
  access: Int,
  name: String,
  desc: String
) : AsmGeneratorAdapter(ASM9, methodVisitor, access, name, desc) {

  constructor(
    methodVisitor: MethodVisitor,
    access: Int,
    method: MethodDescriptor
  ) : this(methodVisitor, access, method.name, method.descriptor)

  constructor(
    classVisitor: ClassVisitor,
    access: Int,
    method: MethodDescriptor
  ) : this(visitMethod(classVisitor, access, method, null, null), access, method)

  constructor(
    classVisitor: ClassVisitor,
    access: Int,
    method: MethodDescriptor,
    signature: String?,
    exceptions: Array<Type.Object>?
  ) : this(visitMethod(classVisitor, access, method, signature, exceptions), access, method)

  fun newArray(type: Type, size: Int) {
    push(size)
    super.newArray(type.toAsmType())
  }

  fun newInstance(type: Type) {
    newInstance(type.toAsmType())
  }

  fun invokeVirtual(owner: Type, method: MethodDescriptor) {
    require(!method.isConstructor) { "Trying to invoke a constructor $method virtually" }
    invoke(INVOKEVIRTUAL, owner, method, false)
  }

  fun invokeConstructor(type: Type, method: MethodDescriptor) {
    require(method.isConstructor) { "Trying to invoke a regular method $method as a constructor" }
    invoke(INVOKESPECIAL, type, method, false)
  }

  fun invokeStatic(owner: Type, method: MethodDescriptor) {
    invoke(INVOKESTATIC, owner, method, false)
  }

  fun invokeInterface(owner: Type, method: MethodDescriptor) {
    require(!method.isConstructor) { "Trying to invoke a constructor $method as an interface method" }
    invoke(INVOKEINTERFACE, owner, method, true)
  }

  fun invokePrivate(owner: Type, method: MethodDescriptor) {
    invoke(INVOKESPECIAL, owner, method, false)
  }

  fun invokeSuper(owner: Type, method: MethodDescriptor) {
    invoke(INVOKESPECIAL, owner, method, false)
  }

  private fun invoke(opcode: Int, type: Type, method: MethodDescriptor, ownerIsInterface: Boolean) {
    val owner = if (type.isArray) type.descriptor else type.internalName
    visitMethodInsn(opcode, owner, method.name, method.descriptor, ownerIsInterface)
  }

  fun getField(owner: Type, field: FieldDescriptor) {
    getField(owner.toAsmType(), field.name, field.type.toAsmType())
  }

  fun putField(owner: Type, field: FieldDescriptor) {
    putField(owner.toAsmType(), field.name, field.type.toAsmType())
  }

  fun getField(owner: Type, name: String, type: Type) {
    getField(owner.toAsmType(), name, type.toAsmType())
  }

  fun putField(owner: Type, name: String, type: Type) {
    putField(owner.toAsmType(), name, type.toAsmType())
  }

  fun getStatic(owner: Type, field: FieldDescriptor) {
    getStatic(owner.toAsmType(), field.name, field.type.toAsmType())
  }

  fun putStatic(owner: Type, field: FieldDescriptor) {
    putStatic(owner.toAsmType(), field.name, field.type.toAsmType())
  }

  fun getStatic(owner: Type, name: String, type: Type) {
    getStatic(owner.toAsmType(), name, type.toAsmType())
  }

  fun putStatic(owner: Type, name: String, type: Type) {
    putStatic(owner.toAsmType(), name, type.toAsmType())
  }

  fun pushNull() {
    visitInsn(ACONST_NULL)
  }

  fun push(type: Type) {
    push(type.toAsmType())
  }

  fun arrayLoad(type: Type) {
    arrayLoad(type.toAsmType())
  }

  fun arrayStore(type: Type) {
    arrayStore(type.toAsmType())
  }

  fun ifCmp(type: Type, mode: Int, label: Label) {
    ifCmp(type.toAsmType(), mode, label)
  }

  fun ifNotCmp(type: Type, mode: Int, label: Label) {
    ifCmp(type.toAsmType(), negateCmpMode(mode), label)
  }

  inline fun ifCmp(type: Type, mode: Int, body: () -> Unit) {
    val label = newLabel()
    ifNotCmp(type, mode, label)
    body()
    mark(label)
  }

  inline fun ifNull(body: () -> Unit) {
    val label = newLabel()
    ifNonNull(label)
    body()
    mark(label)
  }

  inline fun ifNonNull(body: () -> Unit) {
    val label = newLabel()
    ifNull(label)
    body()
    mark(label)
  }

  fun swap(prev: Type, type: Type) {
    swap(prev.toAsmType(), type.toAsmType())
  }

  fun math(op: Int, type: Type) {
    math(op, type.toAsmType())
  }

  fun throwException(type: Type, msg: String) {
    throwException(type.toAsmType(), msg)
  }

  fun checkCast(type: Type) {
    checkCast(type.toAsmType())
  }

  fun box(type: Type) {
    box(type.toAsmType())
  }

  fun valueOf(type: Type) {
    valueOf(type.toAsmType())
  }

  fun unbox(type: Type) {
    unbox(type.toAsmType())
  }

  fun instanceOf(type: Type) {
    instanceOf(type.toAsmType())
  }

  fun newLocal(type: Type): Int {
    return newLocal(type.toAsmType())
  }

  fun visitFrame(type: Int, nLocal: Int, local: Array<Type>?, nStack: Int, stack: Array<Type>?) {
    val localObjects = local?.toFrameObjectArray()
    val stackObjects = stack?.toFrameObjectArray()
    visitFrame(type, nLocal, localObjects, nStack, stackObjects)
  }

  companion object {
    const val EQ: Int = AsmGeneratorAdapter.EQ
    const val NE: Int = AsmGeneratorAdapter.NE
    const val LT: Int = AsmGeneratorAdapter.LT
    const val GE: Int = AsmGeneratorAdapter.GE
    const val LE: Int = AsmGeneratorAdapter.LE
    const val GT: Int = AsmGeneratorAdapter.GT

    private fun visitMethod(
      classVisitor: ClassVisitor,
      access: Int,
      method: MethodDescriptor,
      signature: String?,
      exceptions: Array<Type.Object>?
    ): MethodVisitor {
      return classVisitor.visitMethod(
        access,
        method.name,
        method.descriptor,
        signature,
        exceptions?.mapToArray { it.internalName }
      )
    }

    private fun negateCmpMode(mode: Int): Int {
      return when (mode) {
        EQ -> NE
        NE -> EQ
        LT -> GE
        GE -> LT
        LE -> GT
        GT -> LE
        else -> error("Unknown cmp mode $mode")
      }
    }

    private fun Array<Type>.toFrameObjectArray(): Array<Any>? {
      val objects = ArrayList<Any>(size * 2)
      forEach { type ->
        when (type) {
          is Type.Primitive -> {
            when (type) {
              is Type.Primitive.Boolean,
              is Type.Primitive.Char,
              is Type.Primitive.Byte,
              is Type.Primitive.Short,
              is Type.Primitive.Int -> {
                objects.add(INTEGER)
              }

              is Type.Primitive.Float -> {
                objects.add(FLOAT)
              }

              Type.Primitive.Long -> {
                objects.add(LONG)
                objects.add(TOP)
              }

              Type.Primitive.Double -> {
                objects.add(DOUBLE)
                objects.add(TOP)
              }

              Type.Primitive.Void -> Unit
            }
          }

          is Type.Array -> objects.add(type.descriptor)
          is Type.Object -> objects.add(type.internalName)
          else -> throw IllegalArgumentException("Illegal type used in frame: $type")
        }
      }
      return objects.toArray(arrayOfNulls<Any>(objects.size))
    }
  }
}
