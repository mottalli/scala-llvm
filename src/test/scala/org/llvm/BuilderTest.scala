package org.llvm

import org.scalatest.{BeforeAndAfter, FunSuite}

class BuilderTest extends org.scalatest.FunSuite {
  implicit val context = new Context
  implicit val module = new Module("TestModule")

  test("A function that sums two values") {
    val function = new Function(module.int32Type)(module.int32Type, module.int32Type)("testFunction")
    function.build { implicit builder  =>
      val arg0 = function.args(0).setName("arg0")
      val arg1 = function.args(1).setName("arg1")
      val result = arg0 ~+ arg1
      result.setName("result")
      builder.ret(result)
    }
    val functionStr = function.toString
    assert(functionStr.contains("%result = add i32 %arg0, %arg1"))
    assert(functionStr.contains("ret i32 %result"))
  }

  test("Insertion points can be saved") {
    val function = new Function(module.voidType)(module.int32Type)("testFunction")
    function.build { implicit builder  =>
      val arg = function.args(0).setName("arg")

      builder.pushIP()
      val block1 = function.appendBasicBlock("block1").build { implicit builder =>
        val sum1 = arg ~+ 1;
        sum1.setName("sum1")
        val sum2 = arg ~+ 2;
        sum2.setName("sum2")
      }
      builder.popIP()

      // sum3 should come before sum1 and sum2
      val sum3 = arg ~+ 3; sum3.setName("sum3")
      builder.br(block1)
    }

    val functionStr = function.toString
    assert(functionStr.contains("%sum1 = add i32 %arg, 1"))
    assert(functionStr.contains("%sum3 = add i32 %arg, 3"))
    assert(functionStr.indexOf("%sum3 = add i32 %arg, 3") < functionStr.indexOf("%sum1 = add i32 %arg, 1"))
  }

}
