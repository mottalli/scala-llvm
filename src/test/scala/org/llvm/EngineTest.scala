package org.llvm

import org.scalatest.{FunSuite, BeforeAndAfter}

class EngineTest extends FunSuite with BeforeAndAfter {
  implicit var context: Context = null
  implicit var module: Module = null
  implicit var engine: Engine = null

  before {
    context = new Context()
    module = new Module("TestModule")
    engine = null
  }

  after {
    if (engine != null)
      engine.dispose()
    else
      module.dispose()
    context.dispose()
  }

  def generateValidFunc1: Function = {
    val function = new Function("validFunction1", module.int32Type, module.int32Type, module.int32Type)
    function { implicit builder  =>
      val arg0 = function.args(0) as "arg0"
      val arg1 = function.args(1) as "arg1"
      val result = arg0 + arg1 as "result"
      builder.ret(result)
    }
    function
  }

  def generateInvalidFunc1: Function = {
    // This will generate a type error (int32 + int32 will not return a float)
    val function = new Function("invalidFunction1", module.floatType, module.int32Type, module.int32Type)
    function { implicit builder  =>
      val arg0 = function.args(0) as "arg0"
      val arg1 = function.args(1) as "arg1"
      val result = arg0 + arg1 as "result"
      builder.ret(result)
    }
    function
  }

  test("A valid module can be compiled") {
    generateValidFunc1
    engine = module.compile()
  }

  test("An invalid module throws an exception") {
    generateInvalidFunc1
    intercept[InvalidModuleException](module.compile())
  }

  test("A function pointer can be retrieved") {
    val func1 = generateValidFunc1
    engine = module.compile()
    engine.getCompiledFunction(func1)
  }

  test("A function can be executed") {
    val func1 = generateValidFunc1
    engine = module.compile()
    val compiledFunction = engine.getCompiledFunction(func1)
    val result = compiledFunction(2, 2)
    assert(result.isInstanceOf[Int])
    assert(result == 4)
  }
}
