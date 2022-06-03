package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.url.UrlBuilder;
import com.tambapps.http.hyperpoet.util.Constants;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class HttpPoem extends GroovyObjectSupport {

  @Getter
  @Setter
  private HttpPoet poet;

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
    return body(params, null);
  }

  public Map<?, ?> body(Map<?, ?> params) {
    return body(params, null);
  }

  // b(JSON, queryParam: value)
  public Map<?, ?> b(Map<?, ?> params, ContentType contentType) {
    return body(params, contentType);
  }

  // b(JSON, queryParam: value)
  public Map<?, ?> body(Map<?, ?> params, ContentType contentType) {
    return new BodyMap(params, contentType);
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

  public Map<?, ?> a(Map<?, ?> params) {
    return additionalParameters(params);
  }

  public Map<?, ?> additionalParameters(Map<?, ?> params) {
    return new AdditionalParametersMap(params);
  }

  private Map<?, ?> buildPoetParams(HttpMethod method, Object... params) {
    return buildPoetParams(method.name(), params);
  }

  private Map<?, ?> buildPoetParams(String method, Object... params) {
    Optional<BodyMap> optBody = Optional.empty();
    Optional<ParamMap> optParams = Optional.empty();
    Optional<HeaderMap> optHeaders = Optional.empty();
    Optional<AdditionalParametersMap> optAdditionalParametersMap = Optional.empty();

    Map<Object, Object> poetParams = new HashMap<>();
    boolean requiresRequestBody = method != null && okhttp3.internal.http.HttpMethod.requiresRequestBody(method);
    if (params.length == 1) {
      if (requiresRequestBody) {
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
        } else if (param instanceof AdditionalParametersMap) {
          optAdditionalParametersMap = Optional.of((AdditionalParametersMap) param);
        } else if (param instanceof ContentType) {
          poetParams.put(requiresRequestBody ? Constants.CONTENT_TYPE_PARAM : Constants.ACCEPT_CONTENT_TYPE_PARAM, param);
        }
      }
    }
    optBody.ifPresent(body -> {
      poetParams.put(Constants.BODY_PARAM, body);
      if (body.getContentType() != null) {
        poetParams.put(Constants.CONTENT_TYPE_PARAM, body.getContentType());
      }
    });
    optParams.ifPresent(queryParams -> poetParams.put(Constants.QUERY_PARAMS_PARAM, queryParams));
    optHeaders.ifPresent(headers -> poetParams.put(Constants.HEADER_PARAM, headers));
    optAdditionalParametersMap.ifPresent(poetParams::putAll);
    return poetParams;
  }

  // used to know which map is body
  private static class BodyMap extends HashMap<Object, Object> {
    @Getter
    private final ContentType contentType;
    public BodyMap(Map<?, ?> m, ContentType contentType) {
      super(m);
      this.contentType = contentType;
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

  private static class AdditionalParametersMap extends HashMap<Object, Object> {
    public AdditionalParametersMap(Map<?, ?> m) {
      super(m);
    }
  }

}
