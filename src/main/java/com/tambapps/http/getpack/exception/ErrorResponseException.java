package com.tambapps.http.getpack.exception;

import okhttp3.Response;

import java.io.IOException;

public class ErrorResponseException extends IOException {

  private final Response response;

  public ErrorResponseException(Response response) {
    this.response = response;
  }
}
