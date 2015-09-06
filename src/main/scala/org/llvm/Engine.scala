package org.llvm

case class InvalidModuleException(what: String) extends LLVMException(what)
case class EngineCompilationException(what: String) extends LLVMException(what)
case class InvalidFunctionException(what: String) extends LLVMException(what)

class Engine(val llvmEngine: api.ExecutionEngine) extends Disposable {
  protected def doDispose(): Unit = api.LLVMDisposeExecutionEngine(this)

  def getCompiledFunction(function: Function): CompiledFunction = {
    val fptr = api.tools.LLVMToolsGetPointerToFunction(this, function)
    if (fptr == null)
      throw InvalidFunctionException(s"Cannot retrieve function ${function.name} from compiled module")
    CompiledFunction(function, fptr, this)
  }
}

object Engine {
  implicit def engineToLLVM(engine: Engine): api.ExecutionEngine = engine.llvmEngine
}
