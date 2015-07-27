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
    val function = new Function(module.voidType)(module.int32Type)("testFunction")
    assert(function.toString.contains("declare void @testFunction(i32)"))
  }

  test("We can create global variables") {
    implicit val context = new Context
    implicit val module = new Module("TestModule")

    module.addGlobalVariable(module.int32Type, "globalVar")
    assert(module.toString.contains("@globalVar"))
    println(module)
  }

}
