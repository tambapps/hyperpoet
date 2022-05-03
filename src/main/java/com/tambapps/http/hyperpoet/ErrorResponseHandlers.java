package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.io.parser.JsonParserClosure;
import groovy.lang.Closure;
import okhttp3.Response;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;

public class ErrorResponseHandlers {

  // TODO document addition of this response handler
  public static Closure<?> throwResponseHandler() {
    return new ThrowResponseHandlerClosure();
  }

  // TODO document renaming of this response handler (previously problemResponseHandler())
  public static Closure<?> throwProblemResponseHandler() {
    return throwProblemResponseHandler(new JsonParserClosure());
  }

  public static Closure<?> throwProblemResponseHandler(Closure<?> parser) {
    return new ThrowProblemResponseHandlerClosure(parser);
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

  private static class ThrowProblemResponseHandlerClosure extends Closure<Void> {

    private final Closure<?> jsonParser;

    public ThrowProblemResponseHandlerClosure(Closure<?> jsonParser) {
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
