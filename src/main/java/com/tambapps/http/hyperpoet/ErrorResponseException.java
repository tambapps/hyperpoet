package com.tambapps.http.hyperpoet;

import lombok.Getter;
import okhttp3.Response;

import java.io.IOException;

/**
 * Exception thrown when an error response is received
 */
public class ErrorResponseException extends IOException {

  @Getter
  private final Response response;

  public ErrorResponseException(Response response) {
    this.response = response;
  }

  @Override
  public String toString() {
    return String.format("%d - %s", response.code(), response.message());
  }
}
