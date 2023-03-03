package com.tambapps.http.hyperpoet.io.parser;

import com.tambapps.http.hyperpoet.Function;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;

import java.io.IOException;

public abstract class ParserClosure implements Function {

  protected ParserClosure() {
  }

  @SneakyThrows
  @Override
  public Object call(Object arg) {
    return doCall((ResponseBody) arg);
  }

  protected abstract Object doCall(ResponseBody body) throws IOException;

}