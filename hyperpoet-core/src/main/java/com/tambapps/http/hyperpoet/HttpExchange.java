package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import lombok.Getter;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class HttpExchange {

  @Getter
  private final Response response;
  @Getter
  private final Object requestBody;
  private final Function<Object, ?> parser;

  public HttpExchange(Response response, Object requestBody, Function<Object, ?> parser) {
    if (response != null && response.body() != null && !(response.body() instanceof CachedResponseBody)) {
      throw new IllegalArgumentException("Response body should have been cached");
    }
    this.response = response;
    this.requestBody = requestBody;
    this.parser = parser;
  }

  public boolean isSuccessful() {
    return response != null && response.isSuccessful();
  }

  public Request getRequest() {
    return response.request();
  }

  public RequestBody getRawRequestBody() {
    return getRequest().body();
  }

  public int getResponseCode() {
    return response.code();
  }

  public Map<String, List<String>> getResponseHeaders() {
    return response.headers().toMultimap();
  }

  public Map<String, List<String>> getRequestHeaders() {
    return getRequest().headers().toMultimap();
  }

  public ResponseBody getRawResponseBody() {
    return response.body();
  }

  public Object getResponseBody() {
    if (getRawResponseBody() == null || parser == null) {
      return null;
    } else {
      return parser.apply(getRawResponseBody());
    }
  }

  // for the groovy truth
  public boolean asBoolean() {
    return isSuccessful();
  }

  @Override
  public String toString() {
    return String.format("HttpExchange{request=%s, response=%s}", getRequest(), getResponse());
  }
}