package org.llvm

import org.scalatest.{BeforeAndAfter, FunSuite}

class ModuleTest extends org.scalatest.FunSuite {

  test("A module can be created") {
    implicit val context = new Context
    val module = new Module("TestModule")
    assert(module.toString.contains("; ModuleID = 'TestModule'"))
  }

  test("Types can be mapped") {
    implicit val context = new Context
    implicit val module = new Module("TestModule")

    assert(module.getType[Int] == module.int32Type)
    assert(Type.from[Int] == module.int32Type)
    intercept[UnsupportedTypeException] { module.getType[Module] }
  }

  test("A function can be created") {
    implicit val context = new Context
    implicit val module = new Module("TestModule")
    val function = new Function("testFunction", module.voidType, module.int32Type)
    assert(function.toString.contains("declare void @testFunction(i32)"))
  }

  test("A function that sums two values") {
    implicit val context = new Context
    implicit val module = new Module("TestModule")
    val function = new Function("testFunction", module.int32Type, module.int32Type, module.int32Type)
    function.build { implicit builder  =>
      val arg0 = function.args(0)
      val arg1 = function.args(1)
      val result = arg0 ~+ arg1
      result.setName("result")
      builder.ret(result)
    }
    val functionStr = function.toString
    assert(functionStr.contains("%result = add i32 %arg0, %arg1"))
    assert(functionStr.contains("ret i32 %result"))
  }
}
