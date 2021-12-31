package com.tambapps.http.hyperpoet;

import kotlin.Pair;
import lombok.Getter;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when an error response is received
 */
@Getter
public class ErrorResponseException extends IOException {

  private final int code;
  private final byte[] body;
  private final Map<String, String> headers;

  private ErrorResponseException(String method, String url, int code, byte[] body, Map<String, String> headers) {
    super(String.format("endpoint %s %s response %d", method, url, code));
    this.code = code;
    this.body = body;
    this.headers = headers;
  }

  public static ErrorResponseException from(Response response) throws IOException {
    Map<String, String> headers = new HashMap<>();
    Headers okHeaders = response.headers();
    for (String name : okHeaders.names()) {
      headers.put(name, okHeaders.get(name));
    }
    return new ErrorResponseException(response.request().method(), response.request().url().toString(),
        response.code(), response.body() != null ? response.body().bytes() : null, headers);
  }

  public String getBodyAsText() {
    return new String(body);
  }

}
