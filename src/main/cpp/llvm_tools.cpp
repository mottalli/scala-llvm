/**
 * This library provides some tools to bypass some limitations of LLVM's C API, which makes it difficult
 * to invoke from Scala.
 */
#include "llvm_tools.h"

#include <iostream>
#include <llvm/ExecutionEngine/ExecutionEngine.h>
#include <llvm/IR/IRBuilder.h>

/* ------------------------------------------------------------------------- */
// Useful templates / macros
template<typename R, typename P1>
R _executeFunction1(void* vfptr, P1 p1)
{
    typedef R (*FuncType)(P1);
    FuncType fptr = reinterpret_cast<FuncType>(vfptr);
    return fptr(p1);
}

template<typename R, typename P1, typename P2>
R _executeFunction2(void* vfptr, P1 p1, P2 p2)
{
    typedef R (*FuncType)(P1, P2);
    FuncType fptr = reinterpret_cast<FuncType>(vfptr);
    return fptr(p1, p2);
}

API_EXPORT
{

/* ------------------------------------------------------------------------- */
LLVMBool LLVMToolsCompileModuleWithMCJIT(LLVMExecutionEngineRef *outEngine, LLVMModuleRef module,
                                         unsigned optimizationLevel, char **outError)
{
    LLVMMCJITCompilerOptions options;
    LLVMInitializeMCJITCompilerOptions(&options, sizeof(options));
    options.OptLevel = optimizationLevel;
    return LLVMCreateMCJITCompilerForModule(outEngine, module, &options, sizeof(options), outError);
}

/* ------------------------------------------------------------------------- */
void LLVMToolsInitializeAll()
{
    LLVMInitializeNativeTarget();
    LLVMInitializeNativeAsmPrinter();
    LLVMLinkInMCJIT();
}

/* ------------------------------------------------------------------------- */
void* LLVMToolsGetPointerToFunction(LLVMExecutionEngineRef engineRef, LLVMValueRef functionRef)
{
    llvm::ExecutionEngine* engine = llvm::unwrap(engineRef);
    llvm::Function* function = llvm::unwrap<llvm::Function>(functionRef);
    engine->finalizeObject();           // This HAS to be called! Otherwise the pointer will be invalid.
    return engine->getPointerToFunction(function);
}

/* ------------------------------------------------------------------------- */
typedef llvm::IRBuilder<>::InsertPoint InsertPoint;
LLVMInsertPointRef LLVMSaveInsertPoint(LLVMBuilderRef builderRef)
{
    // Create the insert point in the stack
    InsertPoint ip = llvm::unwrap(builderRef)->saveIP();
    InsertPoint* pip = new InsertPoint(ip.getBlock(), ip.getPoint());
    return (LLVMInsertPointRef) pip;
}

void LLVMRestoreInsertPoint(LLVMBuilderRef builder, LLVMInsertPointRef insertPointRef)
{
    InsertPoint* pip = (InsertPoint*) insertPointRef;
    llvm::unwrap(builder)->restoreIP(*pip);
}

void LLVMDisposeInsertPoint(LLVMInsertPointRef insertPointRef)
{
    InsertPoint* pip = (InsertPoint*) insertPointRef;
    delete pip;
}

/* ------------------------------------------------------------------------- */
int64_t LLVMToolsExecute_L_L_Function(void* vfptr, int64_t p1) { return _executeFunction1<int64_t>(vfptr, p1); }
int32_t LLVMToolsExecute_I_I_Function(void* vfptr, int32_t p1) { return _executeFunction1<int32_t>(vfptr, p1); }
int32_t LLVMToolsExecute_I_II_Function(void* vfptr, int32_t p1, int32_t p2) { return _executeFunction2<int32_t>(vfptr, p1, p2); }
int8_t LLVMToolsExecute_B_II_Function(void* vfptr, int32_t p1, int32_t p2) { return _executeFunction2<int8_t>(vfptr, p1, p2); }
int8_t LLVMToolsExecute_B_FF_Function(void* vfptr, float p1, float p2) { return _executeFunction2<int8_t>(vfptr, p1, p2); }

}   // API_EXPORT
