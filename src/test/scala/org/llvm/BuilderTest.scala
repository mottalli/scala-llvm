package org.llvm

import org.scalatest.{BeforeAndAfter, FunSuite}

class BuilderTest extends FunSuite with BeforeAndAfter {
  implicit var context: Context = null
  implicit var module: Module = null

  before {
    context = new Context
    module = new Module("TestModule")
  }

  after {
    module.dispose()
    context.dispose()
  }

  test("A function that sums two values") {
    val function = Function.create("testFunction", module.int32Type, module.int32Type, module.int32Type)
    function { implicit builder  =>
      val param0 = function.params(0) as "param0"
      val param1 = function.params(1) as "param1"
      val result = param0 + param1 as "result"
      builder.ret(result)
    }
    val functionStr = function.toString
    assert(functionStr.contains("%result = add i32 %param0, %param1"))
    assert(functionStr.contains("ret i32 %result"))
  }

  test("Insertion points can be saved") {
    val function = Function.create("testFunction", module.voidType, module.int32Type)
    function.build { implicit builder  =>
      val param = function.params(0) as "param"

      builder.pushIP()
      val block1 = function.appendBasicBlock("block1") { implicit builder =>
        val sum1 = param + 1 as "sum1"
        val sum2 = param + 2 as "sum2"
      }
      builder.popIP()

      // sum3 should come before sum1 and sum2
      val sum3 = param + 3 as "sum3"
      builder.br(block1)
    }

    val functionStr = function.toString
    assert(functionStr.contains("%sum1 = add i32 %param, 1"))
    assert(functionStr.contains("%sum3 = add i32 %param, 3"))
    assert(functionStr.indexOf("%sum3 = add i32 %param, 3") < functionStr.indexOf("%sum1 = add i32 %param, 1"))
  }

}
