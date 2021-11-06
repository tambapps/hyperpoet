package com.tambapps.http.hyperpoet.url;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;

import java.net.URLEncoder;

/**
 * Represents a single query parameter
 */
@Value
@AllArgsConstructor
public class QueryParam {

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

  @SneakyThrows
  private String urlEncode(Object o) {
    return URLEncoder.encode(String.valueOf(o), "UTF-8");
  }
}