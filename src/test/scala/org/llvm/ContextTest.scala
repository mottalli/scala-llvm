package org.llvm

import org.scalatest.FunSuite

class ContextTest extends FunSuite {
  test("Context can be created and disposed correctly") {
    val context = Context.create()
    assert(Context.contexts.contains(context.llvmContext))

    context.dispose()
    assert(!Context.contexts.contains(context.llvmContext))
  }

  test("Module is kept across types") {
    Context.create().withScope { context =>
      val int32Type = new Int32Type(api.LLVMInt32TypeInContext(context))
      assert(int32Type.context === context)
    }
  }

  test("Types can be cached") {
    Context.create().withScope { context =>
      val context = Context.create()
      val int32Type = new Int32Type(api.LLVMInt32TypeInContext(context))
      assert(context.typeMap.size === 0)

      val resolvedInt32 = context.resolveType(int32Type.llvmType)
      assert(resolvedInt32 === int32Type)
      assert(context.typeMap.size === 1)
    }
  }

  test("Several instances of the same type map to the same object") {
    Context.create().withScope { context =>
      val int32Type1 = new Int32Type(api.LLVMInt32TypeInContext(context))
      val int32Type2 = new Int32Type(api.LLVMInt32TypeInContext(context))
      assert(int32Type1 == int32Type2)

      val resolved1 = context.resolveType(int32Type1.llvmType)
      assert(context.typeMap.size === 1)
      val resolved2 = context.resolveType(int32Type2.llvmType)
      assert(context.typeMap.size === 1)
      assert(resolved1 === resolved2)
    }
  }
}
