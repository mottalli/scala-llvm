package org.llvm

class Context extends Disposable {
  val llvmContext = api.LLVMContextCreate()
  def doDispose() = api.LLVMContextDispose(llvmContext)
}

object Context {
  implicit def contextToLLVM(context: Context): api.Context = context.llvmContext
}

