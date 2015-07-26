package org.llvm

trait Value {
  val llvmValue: api.Value
  implicit val module: Module
  def getType: Type

  override def toString = {
    val ptr = api.LLVMPrintValueToString(this)
    val str = ptr.getString(0)
    api.LLVMDisposeMessage(ptr)
    str
  }

  /** Same-type sum. Will throw error if values are not of the same type */
  def ~+[T](other: T)(implicit builder: Builder): SSAValue = builder.add(this, other)

  /** Sum with upcast */
  def ^+(other: Value)(implicit builder: Builder): SSAValue = ???

  def setName(name: String): this.type = { api.LLVMSetValueName(this, name); this }
}

object Value {
  implicit def valueToLLVM(value: Value): api.Value = value.llvmValue

  /** Helper function that casts constants automatically */
  def from[T](value: T)(implicit module: Module): Value = value match {
    case v: Value => v
    case _        => fromConstant(value)
  }

  def fromConstant[T](value: T)(implicit module: Module): Value = ???
}

abstract class BaseValue(val llvmValue: api.Value, val module: Module) extends Value {
  def getType: Type = module.mapLLVMType(api.LLVMTypeOf(this))
}

class Constant(llvmValue: api.Value)(implicit module: Module) extends BaseValue(llvmValue, module)
class Instruction(llvmValue: api.Value)(implicit module: Module) extends BaseValue(llvmValue, module)
class SSAValue(llvmValue: api.Value)(implicit module: Module) extends Instruction(llvmValue)