package org.llvm

case class BasicBlock(llvmBasicBlock: api.BasicBlock) {
  def build(bodyBuilder: => Unit)(implicit builder: Builder): this.type = {
    builder.pushIP()
    builder.insertAtEndOfBlock(this)
    bodyBuilder
    builder.popIP()
    this
  }
  def :=(bodyBuilder: => Unit)(implicit builder: Builder) : this.type = build(bodyBuilder)(builder)
  def apply(bodyBuilder: => Unit)(implicit builder: Builder) = build(bodyBuilder)(builder)
}

object BasicBlock {
  implicit def basicBlockToLLVM(bb: BasicBlock): api.BasicBlock = bb.llvmBasicBlock
}

