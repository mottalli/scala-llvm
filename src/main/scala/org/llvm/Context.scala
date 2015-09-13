package org.llvm

import collection.mutable.Map

case class InvalidContextException(what: String) extends LLVMException(what)

class Context(val llvmContext: api.Context) extends LLVMObjectWrapper with Disposable {
  val llvmObject: api.GenericObject = llvmContext

  def doDispose() = {
    api.LLVMContextDispose(this)
    Context.unregisterContext(this)
  }

  private[llvm] val typeMap: Map[api.Type, Type] = Map.empty[api.Type, Type]

  def resolveType(theType: api.Type): Type = {
    typeMap.getOrElseUpdate(theType, {
      api.LLVMGetTypeKind(theType) match {
        case api.LLVMIntegerTypeKind => api.LLVMGetIntTypeWidth(theType) match {
          case 32 => new Int32Type(theType)
        }
        case api.LLVMVoidTypeKind => new VoidType(theType)
        case api.LLVMFloatTypeKind => new FloatType(theType)
        case api.LLVMPointerTypeKind => new PointerType(theType)
        case api.LLVMStructTypeKind => new StructType(theType)
        case api.LLVMFunctionTypeKind => new FunctionType(theType)
        case _ => {
          val unknownType = new UnknownType(theType)
          throw new UnsupportedTypeException(s"Cannot resolve type '$unknownType' in context")
        }
      }
    })
  }

  /** Basic types */
  val context = this
  object Types {
    lazy val void = resolveType(api.LLVMVoidTypeInContext(context)).asInstanceOf[VoidType]
    lazy val i32 = resolveType(api.LLVMInt32TypeInContext(context)).asInstanceOf[Int32Type]
    lazy val float = resolveType(api.LLVMFloatTypeInContext(context)).asInstanceOf[FloatType]
  }
}

object Context {
  implicit def contextToLLVM(context: Context): api.Context = context.llvmContext

  val contexts: Map[api.Context, Context] = Map.empty

  def create(): Context = {
    val context = new Context(api.LLVMContextCreate())
    contexts += (context.llvmContext -> context)
    context
  }

  private def unregisterContext(context: Context): Unit = { contexts -= context.llvmContext }

  def resolveContext(context: api.Context): Context = {
    contexts.getOrElse(context, {
      throw InvalidContextException("Could not resolve context")
    })
  }
}

