package org.llvm

case class BasicBlock(llvmBasicBlock: api.BasicBlock) {
  def build(bodyBuilder: Builder => Unit)(implicit builder: Builder): this.type = {
    builder.pushIP()
    builder.insertAtEndOfBlock(this)
    bodyBuilder(builder)
    builder.popIP()
    this
  }
  def apply(bodyBuilder: Builder => Unit)(implicit builder: Builder) = build(bodyBuilder)(builder)
}

object BasicBlock {
  implicit def basicBlockToLLVM(bb: BasicBlock): api.BasicBlock = bb.llvmBasicBlock
}

