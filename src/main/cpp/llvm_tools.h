#pragma once

#include <llvm-c/Core.h>
#include <llvm-c/ExecutionEngine.h>

#define API_EXPORT extern "C"

API_EXPORT {
/* ------------------------------------------------------------------------- */
LLVMBool LLVMToolsCompileModuleWithMCJIT(LLVMExecutionEngineRef *outEngine, LLVMModuleRef module,
                                         unsigned optimizationLevel, char **outError);

/* ------------------------------------------------------------------------- */
void LLVMToolsInitializeAll();

/* ------------------------------------------------------------------------- */
void* LLVMToolsGetPointerToFunction(LLVMExecutionEngineRef engine, LLVMValueRef function);

/* ------------------------------------------------------------------------- */
/** Saves/restores the insertion point for a Builder */
typedef struct LLVMOpaqueInsertPoint* LLVMInsertPointRef;
LLVMInsertPointRef LLVMSaveInsertPoint(LLVMBuilderRef builder);
void LLVMRestoreInsertPoint(LLVMBuilderRef builder, LLVMInsertPointRef insertPoint);
void LLVMDisposeInsertPoint(LLVMInsertPointRef insertPoint);

// Wrappers used to run compiled functions. This involves a combinatorial explosion of parameters!
// The coding for the parameters are:
// B: Byte/Boolean/Char (int8_t)
// S: Short (int16_t)
// I: Int (int32_t)
// L: Long (int64_t)
// F: Float
// D: Double
// P: Pointer
// V: Void (only for return type)
// So, a function named LLVMToolsExecute_D_BS_Function executes a function
// that returns a double and takes an int8_t and an int16_t as paramter

int64_t LLVMToolsExecute_L_L_Function(void* vfptr, int64_t p1);
int32_t LLVMToolsExecute_I_I_Function(void* vfptr, int32_t p1);
int32_t LLVMToolsExecute_I_II_Function(void* vfptr, int32_t p1, int32_t p2);
int8_t LLVMToolsExecute_B_II_Function(void* vfptr, int32_t p1, int32_t p2);
int8_t LLVMToolsExecute_B_FF_Function(void* vfptr, float p1, float p2);

}   // API_EXPORT
