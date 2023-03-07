package com.tambapps.http.hyperpoet.io.parser;

import lombok.SneakyThrows;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.function.Function;

public abstract class ParserClosure implements Function<Object, Object> {

  protected ParserClosure() {
  }

  @SneakyThrows
  @Override
  public Object apply(Object arg) {
    return doCall((ResponseBody) arg);
  }

  protected abstract Object doCall(ResponseBody body) throws IOException;

}