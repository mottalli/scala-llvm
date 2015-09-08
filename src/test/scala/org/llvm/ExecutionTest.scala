package org.llvm

import org.scalatest.{BeforeAndAfter, FunSuite}

class ExecutionTest extends FunSuite with BeforeAndAfter {
  implicit var context: Context = null
  implicit var module: Module = null
  implicit var engine: Engine = null

  before {
    context = Context.create()
    module = Module.create("TestModule")
    engine = null
  }

  after {
    if (engine != null)
      engine.dispose()
    else
      module.dispose()
    context.dispose()
  }

  /*test("A function that counts up to its input") {
    val function = Function.create("testFunction", module.int32Type, module.int32Type)
    function { implicit builder =>
      val limit = function.params(0) as "limit"

      val loopBlock = function.appendBasicBlock("loop")
      val endLoopBlock = function.appendBasicBlock("endLoop")
      val entryBlock = builder.currentBlock

      var counter: PHINode = null

      loopBlock { implicit builder =>
        counter = builder.PHI(limit.getType)
        counter <~ (entryBlock -> 0)

        val nextValue = counter+1

        counter <~ nextValue

        builder.if_(counter >= limit) {
          builder.br(endLoopBlock)
        } else_ {
          builder.br(loopBlock)
        }
      }

      endLoopBlock { implicit builder =>
        builder.ret(counter)
      }
    }
  }*/
}
