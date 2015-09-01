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

  /** Same-type sum. Will throw error if values are not of the same type */
  def ~+[T](other: T)(implicit builder: Builder): SSAValue = builder.add(this, other)

  /** Sum with upcast */
  def ^+(other: Value)(implicit builder: Builder): SSAValue = ???

  def setName(name: String): this.type = { api.LLVMSetValueName(this, name); this }
  def as(name: String): this.type = this.setName(name)
}

object Value {
  implicit def valueToLLVM(value: Value): api.Value = value.llvmValue

  /** Helper function that casts constants automatically */
  def from[T](value: T)(implicit module: Module): Value = value match {
    case v: Value => v
    case _        => fromConstant(value)
  }

  def fromConstant[T](value: T)(implicit module: Module): Constant = value match {
    case v: Int => new Constant(api.LLVMConstInt(module.int32Type, v, 0))
    case _ => ???
  }
}

abstract class BaseValue(val llvmValue: api.Value, val module: Module) extends Value {
}

trait Variable extends Value {
  def fetchValue(implicit builder: Builder): SSAValue = new SSAValue(api.LLVMBuildLoad(builder, this, ""))(builder.module)
  def storeValue[T](v: T)(implicit builder: Builder): Instruction = new Instruction(api.LLVMBuildStore(builder, builder.toVal(v), this))
}

class Constant(llvmValue: api.Value)(implicit module: Module) extends BaseValue(llvmValue, module)
class Instruction(llvmValue: api.Value)(implicit module: Module) extends BaseValue(llvmValue, module)
class SSAValue(llvmValue: api.Value)(implicit module: Module) extends Instruction(llvmValue)
