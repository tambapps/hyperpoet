package com.tambapps.http.hyperpoet;

import lombok.Getter;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * Exception thrown when an error response is received
 */
public class ErrorResponseException extends IOException {

  @Getter
  private final Response response;

  public ErrorResponseException(Response response) {
    super(String.format("%d - %s", response.code(), response.message()));
    this.response = response;
  }

  public int getCode() {
    return response.code();
  }

  public ResponseBody getBody() {
    return response.body();
  }

  public Headers getHeaders() {
    return response.headers();
  }
}
