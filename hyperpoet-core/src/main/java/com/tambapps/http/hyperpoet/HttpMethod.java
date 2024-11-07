package com.tambapps.http.hyperpoet;

public enum HttpMethod {
  POST, GET, PATCH, PUT, DELETE;

  public boolean hasBody() {
    return this == POST || this == PUT || this == PATCH;
  }
}
