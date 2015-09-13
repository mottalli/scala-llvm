package org.llvm

import org.scalatest.{BeforeAndAfter, FunSuite}

class ModuleTest extends FunSuite with BeforeAndAfter {
  implicit var context: Context = null
  implicit var module: Module = null

  before {
    context = Context.create()
    module = null
  }

  after {
    if (module != null)
      module.dispose()
    context.dispose()
  }

  test("A module can be created") {
    module = Module.create("TestModule")
    assert(module.toString.contains("; ModuleID = 'TestModule'"))
  }

  test("A function can be created") {
    module = Module.create("TestModule")

    val void = context.Types.void
    val i32 = context.Types.i32
    val float = context.Types.float

    val function = Function.create("testFunction", void, i32)
    assert(function.name === "testFunction")
    assert(function.toString.contains("declare void @testFunction(i32)"))
    assert(function.params.length === 1)
  }

  test("We can create global variables") {
    module = Module.create("TestModule")
    val i32 = context.Types.i32

    module.addGlobalVariable(i32, "globalVar")
    assert(module.toString.contains("@globalVar"))
  }

  test("We can create, compare and resolve structures") {
    module = Module.create("TestModule")

    val i32 = context.Types.i32
    val float = context.Types.float

    val testStruct1 = module.createStruct("testStruct1", Seq(i32, float))
    assert(testStruct1.name === "testStruct1")

    // If we don't instantiate the struct, it will not be created in the module
    val globalVar1 = module.addGlobalVariable(testStruct1, "globalVar1")
    assert(module.toString.contains("%testStruct1 = type { i32, float }"))

    val testStruct2 = module.createStruct("testStruct2", Seq(i32, float))
    // Even though they are equivalent, these do NOT map to the same type
    assert(testStruct1 !== testStruct2)

    // Test packed structs
    val packedStruct1 = module.createStruct("packedStruct1", Seq(i32, float), true)
    val globalVar2 = module.addGlobalVariable(packedStruct1, "globalVar2")
    assert(module.toString.contains("%packedStruct1 = type <{ i32, float }>"))

    val resolvedType = globalVar1.getType
    assert(resolvedType.isInstanceOf[PointerType])
    assert(resolvedType.asInstanceOf[PointerType].pointedType == testStruct1)

    assert(testStruct1.elements(0).isInstanceOf[Int32Type])
    assert(testStruct1.elements(1).isInstanceOf[FloatType])
    //println(module)
  }
}
