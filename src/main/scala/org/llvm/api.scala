package org.llvm

import com.sun.jna.{Library, Pointer, Native}
import com.sun.jna.ptr.PointerByReference

private[llvm] object api {
  type GenericObject = Pointer
  type Context = Pointer
  type Module = Pointer
  type Value = Pointer
  type Type = Pointer
  type Builder = Pointer
  type FunctionType = Pointer
  type BasicBlock = Pointer
  type ExecutionEngine = Pointer

  val libname = "/usr/local/lib/libLLVM-3.6.so"
  
  Native.register(libname)
  // This has the functions that cannot be loaded using the @native method (for example, functions
  // that accept arrays as input parameters)
  private val nonNative = Native.loadLibrary(libname, classOf[NonNativeApi]).asInstanceOf[NonNativeApi]

  // Enums
  val LLVMIntEq = 32
  val LLVMIntNE = 33
  val LLVMIntSGT = 38
  val LLVMIntSGE = 39
  val LLVMIntSLT = 40
  val LLVMIntSLE = 41

  val LLVMRealOEQ = 1
  val LLVMRealOGT = 2
  val LLVMRealOGE = 3
  val LLVMRealOLT = 4
  val LLVMRealOLE = 5
  val LLVMRealONE = 6
  val LLVMRealORD = 7

  val LLVMVoidTypeKind = 0
  val LLVMHalfTypeKind = 1
  val LLVMFloatTypeKind = 2
  val LLVMDoubleTypeKind = 3
  val LLVMX86_FP80TypeKind = 4
  val LLVMFP128TypeKind = 5
  val LLVMPPC_FP128TypeKind = 6
  val LLVMLabelTypeKind = 7
  val LLVMIntegerTypeKind = 8
  val LLVMFunctionTypeKind = 9
  val LLVMStructTypeKind = 10
  val LLVMPointerTypeKind = 12

  // Context
  @native def LLVMContextCreate(): api.Context
  @native def LLVMContextDispose(context: api.Context)
  @native def LLVMGetTypeContext(typ: api.Type): api.Context

  // Module
  @native def LLVMModuleCreateWithNameInContext(name: String, context: api.Context): api.Module
  @native def LLVMDumpModule(module: api.Module)
  @native def LLVMDisposeModule(module: api.Module)
  // Don't call this -- call tools.LLVMToolsCompileModuleWithMCJIT instead
  //@native def LLVMCreateExecutionEngineForModule(engineRef: PointerByReference, module: api.Module, errorRef: PointerByReference): Int
  @native def LLVMVerifyModule(module: api.Module, action: Int, errorRef: PointerByReference): Int
  @native def LLVMPrintModuleToString(module: api.Module): Pointer
  @native def LLVMDisposeExecutionEngine(engine: api.ExecutionEngine)
  @native def LLVMAddGlobal(module: api.Module, typ: api.Type, name: String): api.Value
  @native def LLVMGetModuleContext(module: api.Module): api.Context

  // Builder
  @native def LLVMCreateBuilderInContext(context: api.Context): api.Builder
  @native def LLVMBuildRet(builder: api.Builder, value: api.Value): api.Value
  @native def LLVMBuildAdd(builder: api.Builder, lhs: api.Value, rhs: api.Value, name: String): api.Value
  @native def LLVMBuildFAdd(builder: api.Builder, lhs: api.Value, rhs: api.Value, name: String): api.Value
  @native def LLVMDisposeBuilder(builder: api.Builder)
  @native def LLVMBuildICmp(builder: api.Builder, predicate: Int, lhs: api.Value, rhs: api.Value, name: String): api.Value
  @native def LLVMBuildFCmp(builder: api.Builder, predicate: Int, lhs: api.Value, rhs: api.Value, name: String): api.Value
  @native def LLVMBuildPhi(builder: api.Builder, phiType: api.Type, name: String): api.Value
  @native def LLVMBuildBr(builder: api.Builder, dest: api.BasicBlock): api.Value
  @native def LLVMBuildCondBr(builder: api.Builder, cond: api.Value, then: api.BasicBlock, otherwise: api.BasicBlock): api.Value
  @native def LLVMBuildLoad(builder: api.Builder, pointerVal: api.Value, name: String): api.Value
  @native def LLVMBuildStore(builder: api.Builder, value: api.Value, pointerVal: api.Value): api.Value
  @native def LLVMGetInsertBlock(builder: api.Builder): api.BasicBlock

  // Builder actions
  @native def LLVMAppendBasicBlockInContext(context: api.Context, function: api.Value, name: String): api.BasicBlock
  @native def LLVMPositionBuilderAtEnd(builder: api.Builder, block: api.BasicBlock)

  // Functions
  def LLVMFunctionType = nonNative.LLVMFunctionType _
  @native def LLVMAddFunction(module: api.Module, name: String, funcType: api.Type): api.Value
  @native def LLVMGetParam(function: api.Value, index: Int): api.Value
  @native def LLVMGetReturnType(functionType: api.Type): api.Type
  @native def LLVMCountParamTypes(functionType: api.Type): Int
  def LLVMGetParamTypes = nonNative.LLVMGetParamTypes _

  // Types
  @native def LLVMVoidTypeInContext(context: api.Context): api.Type
  @native def LLVMInt1TypeInContext(context: api.Context): api.Type
  @native def LLVMInt8TypeInContext(context: api.Context): api.Type
  @native def LLVMInt16TypeInContext(context: api.Context): api.Type
  @native def LLVMInt32TypeInContext(context: api.Context): api.Type
  @native def LLVMInt64TypeInContext(context: api.Context): api.Type
  @native def LLVMGetIntTypeWidth(intType: api.Type): Int
  @native def LLVMFloatTypeInContext(context: api.Context): api.Type
  @native def LLVMDoubleTypeInContext(context: api.Context): api.Type
  @native def LLVMTypeOf(value: api.Value): api.Type
  @native def LLVMPointerType(elementType: api.Type, addressSpace: Int): api.Type
  @native def LLVMGetTypeKind(typ: api.Type): Int
  @native def LLVMPrintTypeToString(module: api.Type): Pointer
  @native def LLVMGetElementType(typ: api.Type): api.Type

  // Structs
  @native def LLVMStructCreateNamed(context: api.Context, name: String): api.Type
  def LLVMStructSetBody = nonNative.LLVMStructSetBody _
  @native def LLVMCountStructElementTypes(struct: api.Type): Int
  def LLVMGetStructElementTypes = nonNative.LLVMGetStructElementTypes _
  @native def LLVMGetStructName(struct: api.Type): String

  // Constants
  @native def LLVMConstInt(intType: api.Type, value: Long, signExtend: Int): api.Value
  @native def LLVMConstReal(realType: api.Type, value: Double): api.Value

  // Values
  @native def LLVMSetValueName(value: api.Value, name: String)
  @native def LLVMPrintValueToString(module: api.Value): Pointer
  @native def LLVMGetValueName(value: api.Value): String

  // Basic block
  @native def LLVMGetBasicBlockTerminator(block: api.BasicBlock): api.Value
  @native def LLVMGetBasicBlockParent(block: api.BasicBlock): api.Value

  // Misc
  @native def LLVMDisposeMessage(message: Pointer)
  def LLVMAddIncoming = nonNative.LLVMAddIncoming _

  // LLVM tools library, used to overcome some limitations of the C api
  object tools {
    Native.register("LLVMTools")

    @native def LLVMToolsInitializeAll()
    @native def LLVMToolsCompileModuleWithMCJIT(outEngineRef: PointerByReference, module: api.Module, optimizationLevel: Int, errorRef: PointerByReference): Int
    @native def LLVMToolsGetPointerToFunction(engine: api.ExecutionEngine, function: api.Value): Pointer

    type InsertPoint = Pointer
    @native def LLVMSaveInsertPoint(builderRef: Builder): InsertPoint
    @native def LLVMRestoreInsertPoint(builderRef: Builder, ip: InsertPoint)
    @native def LLVMDisposeInsertPoint(ip: InsertPoint)

    @native def LLVMToolsExecute_L_L_Function(fptr: Pointer, p1: Long): Long
    @native def LLVMToolsExecute_I_I_Function(fptr: Pointer, p1: Int): Int
    @native def LLVMToolsExecute_I_II_Function(fptr: Pointer, p1: Int, p2: Int): Int
    @native def LLVMToolsExecute_B_II_Function(fptr: Pointer, p1: Int, p2: Int): Byte
    @native def LLVMToolsExecute_B_FF_Function(fptr: Pointer, p1: Float, p2: Float): Byte
  }

  // One-time initialization of LLVM
  tools.LLVMToolsInitializeAll()
}

trait NonNativeApi extends Library {
  def LLVMFunctionType(returnType: api.Type, paramTypes: Array[api.Type], numParams: Int, varArgs: Integer): api.FunctionType
  def LLVMAddIncoming(phiNode: api.Value, incomingValues: Array[api.Value], incomingBlocks: Array[api.BasicBlock], count: Int)

  def LLVMStructSetBody(struct: api.Type, elementTypes: Array[api.Type], elementCount: Int, packed: Boolean)
  def LLVMGetStructElementTypes(structs: api.Type, destTypes: Array[api.Type])

  def LLVMGetParamTypes(functionType: api.Type, destTypes: Array[api.Type])

  /*def LLVMDumpModule(module: api.Module)

  def LLVMCreateGenericValueOfInt(valType: api.Type, value: Long, isSigned: Int): api.GenericValue
  def LLVMGenericValueToInt(value: api.GenericValue, isSigned: Int): Long*/
}
