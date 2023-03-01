package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.io.parser.JsonParserClosure;
import groovy.lang.Closure;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;
import java.util.Collections;

public class ErrorResponseHandlers {

  public static Function throwResponseHandler() {
    return new ThrowResponseHandlerClosure();
  }

  public static Function throwProblemResponseHandler() {
    return throwProblemResponseHandler(new JsonParserClosure());
  }

  public static Function throwProblemResponseHandler(Function parser) {
    return new ThrowProblemResponseHandlerClosure(parser);
  }

  public static Function parseResponseHandler(HttpPoet poet) {
    return (o) -> poet.parseResponse((Response) o, Collections.emptyMap());
  }

  private static class ThrowResponseHandlerClosure implements Function {

    public ThrowResponseHandlerClosure() {
    }

    @SneakyThrows
    @Override
    public Object call(Object arg) {
      doCall((Response) arg);
      return null;
    }
    public void doCall(Response response) throws IOException {
      ErrorResponseException exception = ErrorResponseException.from(response);
      response.close();
      throw exception;
    }
  }

  private static class ThrowProblemResponseHandlerClosure implements Function {

    private final Function jsonParser;

    public ThrowProblemResponseHandlerClosure(Function jsonParser) {
      this.jsonParser = jsonParser;
    }

    @SneakyThrows
    @Override
    public Object call(Object arg) {
      doCall((Response) arg);
      return null;
    }

    public void doCall(Response response) throws IOException {
      ProblemResponseException exception = ProblemResponseException.from(response, jsonParser);
      response.close();
      throw exception;
    }
  }
}
