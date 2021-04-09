package com.tambapps.http.getpack.util;

import lombok.Value;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Value
public class QueryParam {

  String key;
  String value;

  @Override
  public String toString() {
    return key + "=" + value;
  }

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