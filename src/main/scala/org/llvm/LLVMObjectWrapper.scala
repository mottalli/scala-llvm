package org.llvm

trait LLVMObjectWrapper {
  val llvmObject: api.GenericObject

  override def equals(o: Any): Boolean = o match {
    case that: LLVMObjectWrapper => llvmObject == that.llvmObject
    case _ => false
  }
}
