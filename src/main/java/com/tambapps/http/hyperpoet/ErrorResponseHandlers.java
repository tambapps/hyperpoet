package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.io.parser.JsonParserClosure;
import groovy.lang.Closure;
import okhttp3.Response;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;

public class ErrorResponseHandlers {

  public static Closure<?> throwResponseHandler() {
    return new ThrowResponseHandlerClosure();
  }

  public static Closure<?> problemResponseHandler() {
    return problemResponseHandler(new JsonParserClosure());
  }

  public static Closure<?> problemResponseHandler(Closure<?> parser) {
    return new ProblemResponseHandlerClosure(parser);
  }

  public static Closure<?> parseResponseHandler(HttpPoet poet) {
    return new MethodClosure(poet, "parseResponse");
  }

  private static class ThrowResponseHandlerClosure extends Closure<Void> {

    public ThrowResponseHandlerClosure() {
      super(null);
    }

    public void doCall(Response response) throws IOException {
      ErrorResponseException exception = ErrorResponseException.from(response);
      response.close();
      throw exception;
    }
  }

  private static class ProblemResponseHandlerClosure extends Closure<Void> {

    private final Closure<?> jsonParser;

    public ProblemResponseHandlerClosure(Closure<?> jsonParser) {
      super(null);
      this.jsonParser = jsonParser;
    }

    public void doCall(Response response) throws IOException {
      ProblemResponseException exception = ProblemResponseException.from(response, jsonParser);
      response.close();
      throw exception;
    }
  }
}
