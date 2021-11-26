package com.tambapps.http.hyperpoet.io.poeticprinter;

import groovy.lang.Closure;

public abstract class AbstractPoeticPrinter extends Closure<Void> implements PoeticPrinter {

  protected AbstractPoeticPrinter() {
    super(null);
  }

  public void doCall(byte[] bytes) {
    printBytes(bytes);
  }

}
