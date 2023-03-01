package com.tambapps.http.hyperpoet.io.poeticprinter;

import com.tambapps.http.hyperpoet.Function;

public abstract class AbstractPoeticPrinter implements Function, PoeticPrinter {

  protected AbstractPoeticPrinter() {
  }

  @Override
  public Object call(Object arg) {
    doCall((byte[]) arg);
    return null;
  }

  public void doCall(byte[] bytes) {
    printBytes(bytes);
  }

}
