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

  def withScope[T](body: this.type => T): T = {
    val result = body(this)
    this.dispose()
    result
  }
}
