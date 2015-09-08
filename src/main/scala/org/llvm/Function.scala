package org.llvm

class FunctionType(llvmType: api.Type) extends Type(llvmType) {
  lazy val returnType: Type = Type.resolveLLVMType(api.LLVMGetReturnType(this))
  lazy val paramsTypes: Array[Type] = {
    val numParams = api.LLVMCountParamTypes(this)
    val types = new Array[api.Type](numParams)
    api.LLVMGetParamTypes(this, types)
    types.map(Type.resolveLLVMType(_))
  }
}

object FunctionType {
  def create(returnType: Type, paramsTypes: Type*)(implicit module: Module): FunctionType = {
    val llvmArgsTypes: Array[api.Type] = paramsTypes.map(_.llvmType).toArray
    val llvmFunctionType = api.LLVMFunctionType(returnType, llvmArgsTypes, llvmArgsTypes.length, 0)
    new FunctionType(llvmFunctionType)
  }
}

class Function(val llvmValue: api.Value)(implicit val module: Module) extends Value {
  // For some reason, LLVM returns the function type as a pointer to the function
  // type instead of the function type directly
  lazy val functionType: FunctionType = getType.asInstanceOf[PointerType].pointedType.asInstanceOf[FunctionType]
  lazy val returnType = functionType.returnType
  lazy val paramsTypes = functionType.paramsTypes
  lazy val params: Array[Value] = (0 until paramsTypes.length).map(i => new SSAValue(api.LLVMGetParam(this, i))).toArray

  def appendBasicBlock(name: String): BasicBlock = BasicBlock(api.LLVMAppendBasicBlockInContext(module.context, this, name))

  def build(bodyBuilder: Builder => Unit): this.type = {
    val builder = new Builder
    this.appendBasicBlock("init").build(bodyBuilder)(builder)
    builder.dispose()
    this
  }

  def apply(bodyBuilder: Builder => Unit) = build(bodyBuilder)
}

object Function {
  def create(name: String, returnType: Type, paramsTypes: Type*)(implicit module: Module): Function = {
    val functionType = FunctionType.create(returnType, paramsTypes: _*)
    val llvmFunction = api.LLVMAddFunction(module, name, functionType)
    new Function(llvmFunction)
  }
}
