package com.tambapps.http.hyperpoet.util;

import lombok.Value;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Represents a single query parameter
 */
@Value
class QueryParam {

  String key;
  String value;

  @Override
  public String toString() {
    return key + "=" + value;
  }

  /**
   * Returns this query parameter URL encoded
   * @return this query parameter URL encoded
   */
  public String encoded() {
    return urlEncode(key) + "=" + urlEncode(value);
  }

  private String urlEncode(Object o) {
    try {
      return URLEncoder.encode(String.valueOf(o), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Couldn't URL encode", e);
    }
  }
}