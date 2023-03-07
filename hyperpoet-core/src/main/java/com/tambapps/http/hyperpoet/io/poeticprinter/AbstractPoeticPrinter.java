package com.tambapps.http.hyperpoet.io.poeticprinter;

import java.util.function.Function;

public abstract class AbstractPoeticPrinter implements Function<Object, Object>, PoeticPrinter {

  protected AbstractPoeticPrinter() {
  }

  @Override
  public Object apply(Object arg) {
    doCall((byte[]) arg);
    return null;
  }

  public void doCall(byte[] bytes) {
    printBytes(bytes);
  }

}
