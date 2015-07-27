package org.llvm

import reflect.runtime.universe.{TypeTag, typeTag}

class Context {
  val llvmContext = api.LLVMContextCreate()

  def dispose() = api.LLVMContextDispose(llvmContext)
}
object Context {
  implicit def contextToLLVM(context: Context): api.Context = context.llvmContext
}

class Module(val name: String)(implicit val context: Context) extends Disposable {
  val llvmModule = api.LLVMModuleCreateWithNameInContext(name, context)

  def dispose() = api.LLVMDisposeModule(this)

  override def toString = {
    val ptr = api.LLVMPrintModuleToString(this)
    val str = ptr.getString(0)
    api.LLVMDisposeMessage(ptr)
    str
  }

  lazy val voidType = BaseType(api.LLVMVoidTypeInContext(context))
  lazy val int32Type = IntType(api.LLVMInt32TypeInContext(context), 32)

  def getType[T: TypeTag] = typeTag[T] match {
    case NativeVoid.tag => voidType
    case NativeInt32.tag => int32Type
    case _ => throw new UnsupportedTypeException("Could not map type to LLVM type")
  }

  class GlobalVariable(llvmValue: api.Value) extends BaseValue(llvmValue, this) with Variable
  def addGlobalVariable(typ: Type, name: String) = new GlobalVariable(api.LLVMAddGlobal(this, typ, name))
}

object Module {
  implicit def moduleToLLVM(module: Module): api.Module = module.llvmModule
}
