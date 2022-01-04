package com.tambapps.http.hyperpoet.io.parser;

import groovy.lang.Closure;
import okhttp3.ResponseBody;

import java.io.IOException;

public abstract class ParserClosure extends Closure<Object> {

  protected ParserClosure() {
    super(null);
  }

  protected abstract Object doCall(ResponseBody body) throws IOException;

}