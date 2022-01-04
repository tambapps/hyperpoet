package com.tambapps.http.hyperpoet;

import groovy.lang.Closure;
import okhttp3.Response;

import java.io.IOException;

public class ErrorResponseHandlers {

  public static Closure<?> problemResponseHandler() {
    return new ProblemResponseHandlerClosure();
  }

  private static class ProblemResponseHandlerClosure extends Closure<Void> {

    public ProblemResponseHandlerClosure() {
      super(null);
    }

    public void doCall(Response response) throws IOException {
      ErrorResponseException exception = ProblemResponseException.from(response);
      response.close();
      throw exception;
    }
  }
}
