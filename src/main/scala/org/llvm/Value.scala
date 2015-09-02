package org.llvm

trait Value {
  val llvmValue: api.Value
  implicit val module: Module

  override def toString = {
    val ptr = api.LLVMPrintValueToString(this)
    val str = ptr.getString(0)
    api.LLVMDisposeMessage(ptr)
    str
  }

  def getType: Type = Type.resolveLLVMType(api.LLVMTypeOf(this))
  def name: String = api.LLVMGetValueName(this)

  /** Same-type sum. Will throw error if values are not of the same type */
  def +(other: Value)(implicit builder: Builder): SSAValue = builder.add(this, other)

  def setName(name: String): this.type = { api.LLVMSetValueName(this, name); this }
  def as(name: String): this.type = setName(name)
}

object Value {
  implicit def valueToLLVM(value: Value): api.Value = value.llvmValue

  /** Helper function that casts constants automatically */
  implicit def from[T](value: T)(implicit module: Module): Value = value match {
    case v: Value => v
    case _        => fromConstant(value)
  }

  def fromConstant[T](value: T)(implicit module: Module): Constant = value match {
    case v: Int => new Constant(api.LLVMConstInt(module.int32Type, v, 0))
    case v: Float => new Constant(api.LLVMConstReal(module.floatType, v))
    case _ => throw new LLVMException(s"Cannot cast value '$value' to LLVM native value")
  }
}

abstract class BaseValue(val llvmValue: api.Value, val module: Module) extends Value {
}

trait Variable extends Value {
  def valueType: Type = getType.asInstanceOf[PointerType].pointedType
  def fetchValue(implicit builder: Builder): SSAValue = new SSAValue(api.LLVMBuildLoad(builder, this, ""))(builder.module)
  def storeValue(v: Value)(implicit builder: Builder): Instruction = new Instruction(api.LLVMBuildStore(builder, v, this))
}

class Constant(llvmValue: api.Value)(implicit module: Module) extends BaseValue(llvmValue, module)
class Instruction(llvmValue: api.Value)(implicit module: Module) extends BaseValue(llvmValue, module)
class SSAValue(llvmValue: api.Value)(implicit module: Module) extends Instruction(llvmValue)
