package org.llvm

import com.sun.jna.{Function => JNAFunction}
import com.sun.jna.Pointer

case class InvalidNativeFunctionInvocation(what: String) extends LLVMException(what)

case class CompiledFunction(function: Function, fptr: Pointer, engine: Engine) {
  val nativeFunction = JNAFunction.getFunction(fptr)

  def apply(args: Any*): Any = {
    if (args.length != function.paramsTypes.length)
      throw InvalidNativeFunctionInvocation(s"Invalid number of arguments: Function expects ${function.paramsTypes.length} arguments, ${args.length} were provided")

    val boxedArgs: Array[java.lang.Object] = args.zip(function.paramsTypes).map { case (arg, argType) =>
      argType match {
        case _: Int32Type => Int.box(arg.asInstanceOf[Int])
        case _: FloatType => Float.box(arg.asInstanceOf[Float])
        case _ => throw InvalidNativeFunctionInvocation("Cannot cast argument to a primitive type")
      }
    }.toArray

    if (!function.returnType.isInstanceOf[PrimitiveType])
      throw InvalidNativeFunctionInvocation("Return type of function is not a primitive type")

    val returnClass: Class[_] = function.returnType.asInstanceOf[PrimitiveType].primitiveType.runtimeClass
    nativeFunction.invoke(returnClass, boxedArgs)
  }
}
