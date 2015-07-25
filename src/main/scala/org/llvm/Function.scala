package org.llvm

class FunctionType(val returnType: Type, val argsTypes: Type*)(implicit val module: Module) extends Type {
  private val llvmArgsTypes: Seq[api.Type] = argsTypes map { _.llvmType }
  lazy val args: Seq[Value] = (0 until argsTypes.length) map { i => new SSAValue(api.LLVMGetParam(this, i)).setName(s"arg$i") }

  val llvmFunctionType = api.LLVMFunctionType(returnType, llvmArgsTypes.toArray, llvmArgsTypes.length, 0)
  val llvmType = llvmFunctionType
}

class Function(val name: String, returnType: Type, argsTypes: Type*)(implicit val module: Module) extends Value {
  val functionType = new FunctionType(returnType, argsTypes: _*)
  val llvmFunction = api.LLVMAddFunction(module, name, functionType)
  val llvmValue = llvmFunction

  def getType = functionType
}

object Function {
  implicit def functionToLLVM(function: Function): api.Value = function.llvmFunction
}
