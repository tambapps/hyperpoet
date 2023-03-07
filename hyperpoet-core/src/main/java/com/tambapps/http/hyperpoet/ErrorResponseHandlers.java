package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.io.parser.JsonParserClosure;
import lombok.SneakyThrows;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Function;

public class ErrorResponseHandlers {

  public static Function<Object, ?> throwResponseHandler() {
    return new ThrowResponseHandlerClosure();
  }

  public static Function<Object, ?> throwProblemResponseHandler() {
    return throwProblemResponseHandler(new JsonParserClosure());
  }

  public static Function<Object, ?> throwProblemResponseHandler(Function parser) {
    return new ThrowProblemResponseHandlerClosure(parser);
  }

  public static Function<Object, ?> parseResponseHandler(AbstractHttpPoet poet) {
    return (o) -> poet.parseResponse((Response) o, null, null);
  }

  private static class ThrowResponseHandlerClosure implements Function<Object, Object> {

    public ThrowResponseHandlerClosure() {
    }

    @SneakyThrows
    @Override
    public Object apply(Object arg) {
      doCall((Response) arg);
      return null;
    }
    public void doCall(Response response) throws IOException {
      ErrorResponseException exception = ErrorResponseException.from(response);
      response.close();
      throw exception;
    }
  }

  private static class ThrowProblemResponseHandlerClosure implements Function<Object, Object> {

    private final Function<Object, ?> jsonParser;

    public ThrowProblemResponseHandlerClosure(Function<Object, ?> jsonParser) {
      this.jsonParser = jsonParser;
    }

    @SneakyThrows
    @Override
    public Object apply(Object arg) {
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
