package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.invoke.PoeticInvoker;
import com.tambapps.http.hyperpoet.url.UrlBuilder;
import com.tambapps.http.hyperpoet.util.Constants;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class HttpPoem extends GroovyObjectSupport {

  private final HttpPoet poet;

  public Object run(Closure<?> closure) {
    closure.setDelegate(this);
    return closure.call();
  }

  public Object get(String url, Object... params) throws IOException {
    return poet.get(buildPoetParams(HttpMethod.GET, params), url);
  }

  public Object delete(String url, Object... params) throws IOException {
    return poet.delete(buildPoetParams(HttpMethod.DELETE, params), url);
  }

  public Object patch(String url, Object... params) throws IOException {
    return poet.patch(buildPoetParams(HttpMethod.PATCH, params), url);
  }

  public Object put(String url, Object... params) throws IOException {
    return poet.put(buildPoetParams(HttpMethod.PUT, params), url);
  }

  public Object post(String url, Object... params) throws IOException {
    return poet.post(buildPoetParams(HttpMethod.POST, params), url);
  }

  public String path(String path) {
    return path(Collections.emptyMap(), path);
  }

  // url('/endpoint', queryParam: value)
  public String path(Map<?, ?> queryParams, String path) {
    return url(queryParams, path);
  }

  public String url(String url) {
    return url(Collections.emptyMap(), url);
  }

  // url('/endpoint', queryParam: value)
  public String url(Map<?, ?> queryParams, String url) {
    return new UrlBuilder(url).addParams(queryParams).build();
  }

  public Map<?, ?> b(Map<?, ?> params) {
    return body(params);
  }

  public Map<?, ?> body(Map<?, ?> params) {
    return new BodyMap(params);
  }

  public Map<?, ?> h(Map<?, ?> params) {
    return headers(params);
  }

  public Map<?, ?> headers(Map<?, ?> params) {
    return new HeaderMap(params);
  }

  public Map<?, ?> p(Map<?, ?> params) {
    return params(params);
  }

  public Map<?, ?> params(Map<?, ?> params) {
    return new ParamMap(params);
  }

  private Map<?, ?> buildPoetParams(HttpMethod method, Object... params) {
    return buildPoetParams(method.name(), params);
  }

  private Map<?, ?> buildPoetParams(String method, Object... params) {
    Optional<BodyMap> optBody = Optional.empty();
    Optional<ParamMap> optParams = Optional.empty();
    Optional<HeaderMap> optHeaders = Optional.empty();

    Map<Object, Object> poetParams = new HashMap<>();
    if (params.length == 1) {
      if (okhttp3.internal.http.HttpMethod.requiresRequestBody(method)) {
        poetParams.put(Constants.BODY_PARAM, params[0]);
      } else {
        poetParams.put(Constants.QUERY_PARAMS_PARAM, params[0]);
      }
    } else {
      for (Object param : params) {
        if (param instanceof BodyMap) {
          optBody = Optional.of((BodyMap) param);
        } else if (param instanceof ParamMap) {
          optParams = Optional.of((ParamMap) param);
        } else if (param instanceof HeaderMap) {
          optHeaders = Optional.of((HeaderMap) param);
        }
      }
    }
    optBody.ifPresent(body -> poetParams.put(Constants.BODY_PARAM, body));
    optParams.ifPresent(queryParams -> poetParams.put(Constants.QUERY_PARAMS_PARAM, queryParams));
    optHeaders.ifPresent(headers -> poetParams.put(Constants.HEADER_PARAM, headers));
    return poetParams;
  }

  // will be invoked by groovy when method missing
  public void methodMissing(String name, Object args) {
    PoeticInvoker poeticInvoker = poet.getPoeticInvoker();
    if (poeticInvoker != null) {
      // TODO
      // poeticInvoker.invokeOrThrow(poet, name, null);
    }
  }

  // used to know which map is body
  private static class BodyMap extends HashMap<Object, Object> {
    public BodyMap(Map<?, ?> m) {
      super(m);
    }
  }

  private static class ParamMap extends HashMap<Object, Object> {
    public ParamMap(Map<?, ?> m) {
      super(m);
    }
  }

  private static class HeaderMap extends HashMap<Object, Object> {
    public HeaderMap(Map<?, ?> m) {
      super(m);
    }
  }

}
