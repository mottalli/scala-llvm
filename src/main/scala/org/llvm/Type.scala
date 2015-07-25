package org.llvm

import reflect.runtime.universe.{TypeTag, typeTag}

class UnsupportedTypeException(what: String) extends LLVMException(what)

sealed trait NativeType {
  val tag: TypeTag[_]
}

case object NativeVoid extends NativeType {
  val tag = typeTag[Unit]
}

case object NativeInt32 extends NativeType {
  val tag = typeTag[Int]
}

abstract class Type {
  val llvmType: api.Type

  override def toString = {
    val ptr = api.LLVMPrintTypeToString(this)
    val str = ptr.getString(0)
    api.LLVMDisposeMessage(ptr)
    str
  }
}

object Type {
  implicit def typeToLLVM(t: Type): api.Type = t.llvmType

  def from[T: TypeTag](implicit module: Module) = module.getType[T]
}

case class BaseType(val llvmType: api.Type) extends Type
case class IntType(val llvmType: api.Type, val size: Int) extends Type
