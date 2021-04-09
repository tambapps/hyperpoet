package com.tambapps.http.getpack;

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
}
