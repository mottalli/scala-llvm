package org.llvm

import collection.mutable.Stack

class Builder(implicit val module: Module) extends Disposable {
  val llvmBuilder = api.LLVMCreateBuilderInContext(module.context)

  protected def doDispose() = api.LLVMDisposeBuilder(this)

  def add(v1: Value, v2: Value): SSAValue = new SSAValue(api.LLVMBuildAdd(this, v1, v2, ""))

  val insertPointStack = new Stack[api.tools.InsertPoint]
  def pushIP() = insertPointStack.push(api.tools.LLVMSaveInsertPoint(this))
  def popIP() = {
    val ip: api.tools.InsertPoint = insertPointStack.pop()
    api.tools.LLVMRestoreInsertPoint(this, ip)
    api.tools.LLVMDisposeInsertPoint(ip)
  }

  def insertAtEndOfBlock(block: BasicBlock) = api.LLVMPositionBuilderAtEnd(this, block)

  def ret(v: Value): Instruction = new Instruction(api.LLVMBuildRet(this, v))
  def br(destBlock: BasicBlock): Instruction = new Instruction(api.LLVMBuildBr(this, destBlock))
}

object Builder {
  implicit def builderToLLVM(builder: Builder): api.Builder = builder.llvmBuilder
}
