package org.llvm

import collection.mutable.Stack

class Builder(implicit val module: Module) extends Disposable {
  val llvmBuilder = api.LLVMCreateBuilderInContext(module.context)

  def dispose() = api.LLVMDisposeBuilder(this)

  def add[T1, T2](v1: T1, v2: T2): SSAValue = new SSAValue(api.LLVMBuildAdd(this, toVal(v1), toVal(v2), ""))

  private def toVal[T](v: T): Value = Value.from(v)

  val insertPointStack = new Stack[api.tools.InsertPoint]
  def pushIP() = insertPointStack.push(api.tools.LLVMSaveInsertPoint(this))
  def popIP() = {
    val ip: api.tools.InsertPoint = insertPointStack.pop()
    api.tools.LLVMRestoreInsertPoint(this, ip)
    api.tools.LLVMDisposeInsertPoint(ip)
  }

  def ret[T](v: T): Instruction = new Instruction(api.LLVMBuildRet(this, toVal(v)))
}

object Builder {
  implicit def builderToLLVM(builder: Builder): api.Builder = builder.llvmBuilder
}
