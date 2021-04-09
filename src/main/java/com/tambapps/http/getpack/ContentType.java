package com.tambapps.http.getpack;

import lombok.Getter;
import okhttp3.MediaType;

/**
 * Enum representing many media content types
 */
public enum ContentType {
  TEXT("text/plain"),
  JSON("application/json"),
  XML("application/xml"),
  HTML("text/html"),
  BINARY("application/octet-stream");

  @Getter
  private final MediaType mediaType;

  ContentType(String headerValue) {
    this.mediaType = MediaType.get(headerValue);
  }
}
