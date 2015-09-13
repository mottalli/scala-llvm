package org.llvm

import collection.mutable.Stack

class Builder(implicit val module: Module) extends Disposable {
  val llvmBuilder = api.LLVMCreateBuilderInContext(module.context)
  private val NO_NAME: String = ""

  protected def doDispose() = api.LLVMDisposeBuilder(this)

  def add(v1: Value, v2: Value): SSAValue = new SSAValue(api.LLVMBuildAdd(this, v1, v2, NO_NAME))

  val insertPointStack = new Stack[api.tools.InsertPoint]
  def pushIP() = insertPointStack.push(api.tools.LLVMSaveInsertPoint(this))
  def popIP() = {
    val ip: api.tools.InsertPoint = insertPointStack.pop()
    api.tools.LLVMRestoreInsertPoint(this, ip)
    api.tools.LLVMDisposeInsertPoint(ip)
  }

  def insertAtEndOfBlock(block: BasicBlock) = api.LLVMPositionBuilderAtEnd(this, block)

  def currentBlock: BasicBlock = BasicBlock(api.LLVMGetInsertBlock(this))
  def currentFunction: Function = new Function(api.LLVMGetBasicBlockParent(api.LLVMGetInsertBlock(this)))

  def ret(v: Value): Instruction = new Instruction(api.LLVMBuildRet(this, v))
  def br(destBlock: BasicBlock): Instruction = new Instruction(api.LLVMBuildBr(this, destBlock))
  def condBr(condition: Value, trueBlock: BasicBlock, falseBlock: BasicBlock): Instruction =
    new Instruction(api.LLVMBuildCondBr(this, condition, trueBlock, falseBlock))
  def PHI(t: Type): PHINode = new PHINode(api.LLVMBuildPhi(this, t, NO_NAME))

  def getBasicBlockTerminator(block: BasicBlock): Option[Instruction] = api.LLVMGetBasicBlockTerminator(block) match {
    case null => None
    case value => Some(new Instruction(value))
  }
}

object Builder {
  implicit def builderToLLVM(builder: Builder): api.Builder = builder.llvmBuilder
}
