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

case object NativeFloat extends NativeType {
  val tag = typeTag[Float]
}

abstract class Type {
  val llvmType: api.Type

  override def toString = {
    val ptr = api.LLVMPrintTypeToString(this)
    val str = ptr.getString(0)
    api.LLVMDisposeMessage(ptr)
    str
  }

  override def equals(o: Any) = o match {
    case that: Type => llvmType == that.llvmType
    case _ => false
  }

  override def hashCode = llvmType.hashCode
}

object Type {
  implicit def typeToLLVM(t: Type): api.Type = t.llvmType

  private[llvm] def resolveLLVMType(theType: api.Type): Type = api.LLVMGetTypeKind(theType) match {
    case api.LLVMIntegerTypeKind => api.LLVMGetIntTypeWidth(theType) match {
      case 32 => Int32Type(theType)
    }
    case api.LLVMFloatTypeKind => FloatType(theType)
    case api.LLVMPointerTypeKind => PointerType(theType)
    case api.LLVMStructTypeKind => StructType(theType)
    case _ => {
      val unknownType = new Type { override val llvmType: api.Type = theType }
      throw new UnsupportedTypeException(s"Cannot resolve type '$unknownType'")
    }
  }
}

case class VoidType(val llvmType: api.Type) extends Type
case class FloatType(val llvmType: api.Type) extends Type
case class Int32Type(val llvmType: api.Type) extends Type

case class StructType(val llvmType: api.Type) extends Type {
  def elements: Seq[Type] = {
    val numElements = api.LLVMCountStructElementTypes(this)
    val llvmTypes = new Array[api.Type](numElements)
    api.LLVMGetStructElementTypes(this, llvmTypes)
    llvmTypes.map { Type.resolveLLVMType }
  }
}

case class PointerType(val llvmType: api.Type) extends Type {
  def pointedType: Type = Type.resolveLLVMType(api.LLVMGetElementType(this))
}
