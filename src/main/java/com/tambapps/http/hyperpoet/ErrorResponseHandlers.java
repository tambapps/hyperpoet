package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.io.parser.JsonParserClosure;
import groovy.lang.Closure;
import okhttp3.Response;

import java.io.IOException;

public class ErrorResponseHandlers {

  public static Closure<?> problemResponseHandler() {
    return problemResponseHandler(new JsonParserClosure());
  }
  public static Closure<?> problemResponseHandler(Closure<?> parser) {
    return new ProblemResponseHandlerClosure(parser);
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
