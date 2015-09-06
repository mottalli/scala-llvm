package org.llvm

import org.llvm.Builder.IfBuilder

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
  def currentFunction: Function = ???

  def ret(v: Value): Instruction = new Instruction(api.LLVMBuildRet(this, v))
  def br(destBlock: BasicBlock): Instruction = new Instruction(api.LLVMBuildBr(this, destBlock))
  def condBr(condition: Value, trueBlock: BasicBlock, falseBlock: BasicBlock): Instruction =
    new Instruction(api.LLVMBuildCondBr(this, condition, trueBlock, falseBlock))
  def PHI(t: Type): PHINode = new PHINode(api.LLVMBuildPhi(this, t, NO_NAME))

  def getBasicBlockTerminator(block: BasicBlock): Option[Instruction] = api.LLVMGetBasicBlockTerminator(block) match {
    case null => None
    case value => Some(new Instruction(value))
  }

  def if_(condition: Value)(ifBody: () => Unit): Builder.IfBuilder = {
    val ifBuilder = new IfBuilder(this)
    ifBuilder.if_(condition, ifBody)
  }
}

object Builder {
  implicit def builderToLLVM(builder: Builder): api.Builder = builder.llvmBuilder

  class IfBuilder(builder: Builder, blockPrefix: String="") {
    val ifBlock: BasicBlock = builder.currentFunction.appendBasicBlock(s"${blockPrefix}_if")
    val elseBlock: BasicBlock = builder.currentFunction.appendBasicBlock(s"${blockPrefix}_else")

    def if_(condition: Value, ifBody: () => Unit, ifBlockName: String = "_ifBlock"): this.type = {
      builder.condBr(condition, ifBlock, elseBlock)
      builder.insertAtEndOfBlock(ifBlock)
      ifBody()

      // Check if the block was finished with a terminator instruction. If it wasn't, jump to the else() block
      builder.getBasicBlockTerminator(ifBlock).getOrElse {
        builder.br(elseBlock)
      }

      builder.insertAtEndOfBlock(elseBlock)
      this
    }

    def else_(elseBody: () => Unit): Unit = {
      builder.insertAtEndOfBlock(elseBlock)
      elseBody()

      builder.getBasicBlockTerminator(elseBlock).getOrElse {
        val endifBlock = builder.currentFunction.appendBasicBlock(s"${blockPrefix}_endif")
        builder.br(endifBlock)
        builder.insertAtEndOfBlock(endifBlock)
      }
    }
  }
}
