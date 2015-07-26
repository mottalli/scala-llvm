package org.llvm

class BasicBlock(val name: String, val function: Function) {
  val llvmBasicBlock = api.LLVMAppendBasicBlockInContext(function.module.context, function, name)

  def build(bodyBuilder: Builder => Unit)(implicit builder: Builder): this.type = {
    builder.pushIP()
    api.LLVMPositionBuilderAtEnd(builder, this)
    bodyBuilder(builder)
    builder.popIP()
    this
  }

}

object BasicBlock {
  implicit def basicBlockToLLVM(bb: BasicBlock): api.BasicBlock = bb.llvmBasicBlock
}

class FunctionType(val returnType: Type, val argsTypes: Type*)(implicit val module: Module) extends Type {
  private val llvmArgsTypes: Seq[api.Type] = argsTypes map { _.llvmType }

  val llvmFunctionType = api.LLVMFunctionType(returnType, llvmArgsTypes.toArray, llvmArgsTypes.length, 0)
  val llvmType = llvmFunctionType
}

class Function(val name: String, returnType: Type, argsTypes: Type*)(implicit val module: Module) extends Value {
  val functionType = new FunctionType(returnType, argsTypes: _*)
  val llvmFunction = api.LLVMAddFunction(module, name, functionType)
  val llvmValue = llvmFunction
  lazy val args: Array[Value] = (0 until argsTypes.length) map { i => new SSAValue(api.LLVMGetParam(this, i)).setName(s"arg$i") } toArray

  def getType = functionType

  def appendBasicBlock(name: String): BasicBlock = new BasicBlock(name, this)

  def build(bodyBuilder: Builder => Unit): this.type = {
    val builder = new Builder
    this.appendBasicBlock("init").build(bodyBuilder)(builder)
    this
  }
}

object Function {
  implicit def functionToLLVM(function: Function): api.Value = function.llvmFunction
}
