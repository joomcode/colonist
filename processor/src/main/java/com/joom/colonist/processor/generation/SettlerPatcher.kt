package com.joom.colonist.processor.generation

import com.joom.colonist.processor.descriptors.MethodDescriptor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PROTECTED
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ASM5

class SettlerPatcher(classVisitor: ClassVisitor) : ClassVisitor(ASM5, classVisitor) {

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<String>?) {
    val newAccess = makeAccessPublic(access)
    super.visit(version, newAccess, name, signature, superName, interfaces)
  }

  override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
    val newAccess = if (MethodDescriptor.isDefaultConstructor(name, descriptor)) makeAccessPublic(access) else access
    return super.visitMethod(newAccess, name, descriptor, signature, exceptions)
  }

  private fun makeAccessPublic(access: Int): Int {
    return (access and (ACC_PRIVATE or ACC_PROTECTED).inv()) or ACC_PUBLIC
  }
}
