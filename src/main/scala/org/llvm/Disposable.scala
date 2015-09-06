package org.llvm

trait Disposable {
  private var disposed: Boolean = false

  def dispose(): Unit = {
    if (disposed)
      throw new LLVMException("Tried to dispose LLVM object twice")

    doDispose()
    disposed = true
  }

  protected def doDispose(): Unit
}
