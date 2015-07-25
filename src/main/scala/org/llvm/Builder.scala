package org.llvm

private[llvm] class Builder(implicit val module: Module) extends Disposable {
  val llvmBuilder = api.LLVMCreateBuilderInContext(module.context)

  def dispose() = api.LLVMDisposeBuilder(this)

  def add[T1, T2](v1: T1, v2: T2): SSAValue = new SSAValue(api.LLVMBuildAdd(this, toVal(v1), toVal(v2), "sum"))

  private def toVal[T](v: T): Value = Value.from(v)
}

object Builder {
  implicit def builderToLLVM(builder: Builder): api.Builder = builder.llvmBuilder
}
