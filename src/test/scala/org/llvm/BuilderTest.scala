package org.llvm

import org.scalatest.{BeforeAndAfter, FunSuite}

class BuilderTest extends org.scalatest.FunSuite {
  implicit val context = new Context
  implicit val module = new Module("TestModule")

  test("A function that sums two values") {
    val function = new Function("testFunction", module.int32Type, module.int32Type, module.int32Type)
    function { implicit builder  =>
      val arg0 = function.args(0) as "arg0"
      val arg1 = function.args(1) as "arg1"
      val result = arg0 + arg1 as "result"
      builder.ret(result)
    }
    val functionStr = function.toString
    assert(functionStr.contains("%result = add i32 %arg0, %arg1"))
    assert(functionStr.contains("ret i32 %result"))
  }

  test("Insertion points can be saved") {
    val function = new Function("testFunction", module.voidType, module.int32Type)
    function.build { implicit builder  =>
      val arg = function.args(0) as "arg"

      builder.pushIP()
      val block1 = function.appendBasicBlock("block1") { implicit builder =>
        val sum1 = arg + 1 as "sum1"
        val sum2 = arg + 2 as "sum2"
      }
      builder.popIP()

      // sum3 should come before sum1 and sum2
      val sum3 = arg + 3 as "sum3"
      builder.br(block1)
    }

    val functionStr = function.toString
    assert(functionStr.contains("%sum1 = add i32 %arg, 1"))
    assert(functionStr.contains("%sum3 = add i32 %arg, 3"))
    assert(functionStr.indexOf("%sum3 = add i32 %arg, 3") < functionStr.indexOf("%sum1 = add i32 %arg, 1"))
  }

}
