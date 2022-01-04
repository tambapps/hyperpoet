package com.tambapps.http.hyperpoet;

import lombok.Getter;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Exception thrown when an error response is received
 */
@Getter
public class ErrorResponseException extends RuntimeException {

  private final int code;
  private final byte[] body;
  private final Map<String, String> headers;

  protected ErrorResponseException(String method, String url, int code, byte[] body, Map<String, String> headers) {
    this(code, body, headers, String.format("endpoint %s %s response %d", method, url, code));
  }

  protected ErrorResponseException(int code, byte[] body, Map<String, String> headers, String message) {
    super(message);
    this.code = code;
    this.body = body;
    this.headers = headers;
  }

  public String getBodyAsText() {
    return new String(body);
  }

  public <T> T getBody(Function<byte[], T> converter) {
    return converter.apply(body);
  }

  public static ErrorResponseException from(Response response) throws IOException {
    return new ErrorResponseException(response.request().method(), response.request().url().toString(),
        response.code(), response.body() != null ? response.body().bytes() : null, getHeaders(response));
  }

  protected static Map<String, String> getHeaders(Response response) {
    Map<String, String> headers = new HashMap<>();
    Headers okHeaders = response.headers();
    for (String name : okHeaders.names()) {
      headers.put(name, okHeaders.get(name));
    }
    return headers;
  }
}
