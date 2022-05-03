package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.url.UrlBuilder;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class HttpPoem extends GroovyObjectSupport {

  private final HttpPoet poet;

  public void run(Closure<?> closure) {
    closure.setDelegate(this);
    closure.call();
  }

  public Object get(String url) throws IOException {
    return poet.get(url);
  }

  public Object get(String url, Map<?, ?> headers) throws IOException {
    return poet.get(map("headers", headers), url);
  }

  public Object delete(String url) throws IOException {
    return poet.delete(url);
  }
  public Object delete(String url, Map<?, ?> headers) throws IOException {
    return poet.delete(map("headers", headers), url);
  }

  public Object patch(String url, Object body) throws IOException {
    return poet.patch(map("body", body), url);
  }

  public Object patch(String url, Object body, Map<?, ?> headers) throws IOException {
    return poet.patch(map("body", body, "headers", headers), url);
  }

  public Object put(String url, Object body) throws IOException {
    return poet.put(map("body", body), url);
  }

  public Object put(String url, Object body, Map<?, ?> headers) throws IOException {
    return poet.put(map("body", body, "headers", headers), url);
  }

  public Object post(String url, Object body) throws IOException {
    return poet.post(map("body", body), url);
  }

  public Object post(String url, Object body, Map<?, ?> headers) throws IOException {
    return poet.post(map("body", body, "headers", headers), url);
  }

  // url('/endpoint', queryParam: value)
  public String path(String path) {
    return path(Collections.emptyMap(), path);
  }

  public String path(Map<?, ?> queryParams, String path) {
    return url(queryParams, path);
  }

    public String url(String url) {
    return url(Collections.emptyMap(), url);
  }

  public String url(Map<?, ?> queryParams, String url) {
    return new UrlBuilder(url).addParams(queryParams).build();
  }

  private static Map<?,?> map(Object... values) {
    Map<Object, Object> map = new HashMap<>();
    for (int i = 0; i < values.length / 2; i++) {
      map.put(values[i / 2], values[i / 2 + 1]);
    }
    return map;
  }
}
