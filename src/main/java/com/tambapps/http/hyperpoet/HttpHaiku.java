package com.tambapps.http.hyperpoet;

import static com.tambapps.http.hyperpoet.util.Constants.ACCEPT_CONTENT_TYPE_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.BODY_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.COMPOSER_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.CONTENT_TYPE_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.HEADER_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.PARSER_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.QUERY_PARAMS_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.SKIP_HISTORY_PARAM;

import groovy.lang.Closure;
import groovy.transform.NamedParam;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class allowing you to perform quick one-time requests
 */
// TODO document this
public class HttpHaiku {

  private static final AtomicReference<HttpPoet> POET_REFERENCE = new AtomicReference<>();

  public static HttpPoet getPoet() {
    return POET_REFERENCE.updateAndGet(p -> p != null ? p : new HttpPoet());
  }

  public static HttpPoem toPoem() {
    return getPoet().poem();
  }

  public static Object get(String url) throws IOException {
    return get(url, Collections.emptyMap());
  }

  public static Object get(String url, Map queryParams) throws IOException {
    return get(Collections.singletonMap(QUERY_PARAMS_PARAM, queryParams), url);
  }

  public static Object get(
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String url) throws IOException {
    return getPoet().get(additionalParameters, url);
  }

  public static Object delete(String url) throws IOException {
    return delete(url, Collections.emptyMap());
  }

  public static Object delete(String url, Map queryParams) throws IOException {
    return delete(Collections.singletonMap(QUERY_PARAMS_PARAM, queryParams), url);
  }
  public static Object delete(
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String url) throws IOException {
    return getPoet().delete(additionalParameters, url);
  }

  public static Object post(String url, Map body) throws IOException {
    return post(Collections.singletonMap(BODY_PARAM, body), url);
  }

  public static Object post(String url, Map body, ContentType contentType) throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put(BODY_PARAM, body);
    map.put(CONTENT_TYPE_PARAM, contentType);
    return post(map, url);
  }

  public static Object post(
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String url) throws IOException {
    return getPoet().post(additionalParameters, url);
  }

  public static Object patch(String url, Map body) throws IOException {
    return patch(Collections.singletonMap(BODY_PARAM, body), url);
  }

  public static Object patch(String url, Map body, ContentType contentType) throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put(BODY_PARAM, body);
    map.put(CONTENT_TYPE_PARAM, contentType);
    return patch(map, url);
  }

  public static Object patch(
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String url) throws IOException {
    return getPoet().patch(additionalParameters, url);
  }

  public static Object put(String url, Map body) throws IOException {
    return put(Collections.singletonMap(BODY_PARAM, body), url);
  }

  public static Object put(String url, Map body, ContentType contentType) throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put(BODY_PARAM, body);
    map.put(CONTENT_TYPE_PARAM, contentType);
    return put(map, url);
  }

  public static Object put(
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String url) throws IOException {
    return getPoet().put(additionalParameters, url);
  }

}
