package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.io.parser.JsonParserClosure;
import lombok.SneakyThrows;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Function;

public class ErrorResponseHandlers {

  public static ErrorResponseHandler throwResponseHandler() {
    return new ThrowResponseHandlerClosure();
  }

  public static ErrorResponseHandler throwProblemResponseHandler() {
    return throwProblemResponseHandler(new JsonParserClosure());
  }

  public static ErrorResponseHandler throwProblemResponseHandler(Function parser) {
    return new ThrowProblemResponseHandlerClosure(parser);
  }

  public static ErrorResponseHandler parseResponseHandler() {
    return new ParseResponseHandlerClosure();
  }

  private static class ParseResponseHandlerClosure implements ErrorResponseHandler {

    private AbstractHttpPoet poet;

    @Override
    public void init(AbstractHttpPoet poet) {
      this.poet = poet;
    }

    @Override
    public Object handle(Response response) {
      return poet.parseResponse(response, null, null);
    }

    public void doCall(Response response) throws IOException {
      ErrorResponseException exception = ErrorResponseException.from(response);
      response.close();
      throw exception;
    }
  }

  private static class ThrowResponseHandlerClosure implements ErrorResponseHandler {

    public ThrowResponseHandlerClosure() {
    }

    @SneakyThrows
    @Override
    public Object handle(Response response) {
      doCall(response);
      return null;
    }
    public void doCall(Response response) throws IOException {
      ErrorResponseException exception = ErrorResponseException.from(response);
      response.close();
      throw exception;
    }
  }

  private static class ThrowProblemResponseHandlerClosure implements ErrorResponseHandler {

    private final Function<Object, ?> jsonParser;

    public ThrowProblemResponseHandlerClosure(Function<Object, ?> jsonParser) {
      this.jsonParser = jsonParser;
    }

    @SneakyThrows
    @Override
    public Object handle(Response response) {
      doCall(response);
      return null;
    }

    public void doCall(Response response) throws IOException {
      ProblemResponseException exception = ProblemResponseException.from(response, jsonParser);
      response.close();
      throw exception;
    }
  }
}
