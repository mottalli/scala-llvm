package org.llvm

import reflect.runtime.universe.{TypeTag, typeTag}

class UnsupportedTypeException(what: String) extends LLVMException(what)

abstract class Type {
  val llvmType: api.Type

  override def equals(o: Any) = o match {
    case that: Type => (llvmType == that.llvmType)
    case _ => false
  }

  override def toString = {
    val ptr = api.LLVMPrintTypeToString(this)
    val str = ptr.getString(0)
    api.LLVMDisposeMessage(ptr)
    str
  }

  def pointerTo: PointerType = PointerType(api.LLVMPointerType(this, 0))
}

trait PrimitiveType extends Type {
  val primitiveType: Manifest[_]
}

object Type {
  implicit def typeToLLVM(t: Type): api.Type = t.llvmType

  private[llvm] def resolveLLVMType(theType: api.Type): Type = LLVMTypeResolver.resolveType(theType)
}

object LLVMTypeResolver {
  import scala.collection.mutable.Map
  private val typeMap: Map[api.Type, Type] = Map.empty[api.Type, Type]

  def resolveType(theType: api.Type): Type = {
    typeMap.getOrElseUpdate(theType, {
      api.LLVMGetTypeKind(theType) match {
        case api.LLVMIntegerTypeKind => api.LLVMGetIntTypeWidth(theType) match {
          case 32 => Int32Type(theType)
        }
        case api.LLVMFloatTypeKind => FloatType(theType)
        case api.LLVMPointerTypeKind => PointerType(theType)
        case api.LLVMStructTypeKind => StructType(theType)
        case api.LLVMFunctionTypeKind => FunctionType(theType)
        case _ => {
          val unknownType = new Type { override val llvmType: api.Type = theType }
          throw new UnsupportedTypeException(s"Cannot resolve type '$unknownType'")
        }
      }
    })
  }
}

case class VoidType(val llvmType: api.Type) extends PrimitiveType {
  val primitiveType: Manifest[Unit] = manifest[Unit]
}

case class FloatType(val llvmType: api.Type) extends PrimitiveType {
  val primitiveType: Manifest[Float] = manifest[Float]
}

case class Int32Type(val llvmType: api.Type) extends PrimitiveType {
  val primitiveType: Manifest[Int] = manifest[Int]
}

case class StructType(val llvmType: api.Type) extends Type {
  def elements: Seq[Type] = {
    val numElements = api.LLVMCountStructElementTypes(this)
    val llvmTypes = new Array[api.Type](numElements)
    api.LLVMGetStructElementTypes(this, llvmTypes)
    llvmTypes.map { Type.resolveLLVMType }
  }

  def name: String = api.LLVMGetStructName(this)
}

case class PointerType(val llvmType: api.Type) extends Type {
  def pointedType: Type = Type.resolveLLVMType(api.LLVMGetElementType(this))
}
