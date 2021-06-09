package com.tambapps.http.hyperpoet.util;

import com.tambapps.http.hyperpoet.io.QueryParamComposers;
import groovy.lang.Closure;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Util class to build URLs. It takes care of the URL encoding with the encoded() method
 */
@Getter
public class UrlBuilder {

  /**
   * Enum representing the way to handle query param list,sets
   */
  public enum QueryParamListComposingType {
    /**
     * Use brackets and separate elements with a comma
     */
    BRACKETS,
    /**
     * separate elements with a comma
     */
    COMMA,
    /**
     * Repeat the parameter for each element of the list
     */
    REPEAT
  }
  private String url;
  private final Map<Class<?>, Closure<?>> queryParamComposers;
  private final List<QueryParam> queryParams = new ArrayList<>();
  private final QueryParamListComposingType queryParamListComposingType;

  public UrlBuilder() {
    this("");
  }

  public UrlBuilder(String url) {
    this(url, QueryParamComposers.getMap());
  }

  public UrlBuilder(String url, Map<Class<?>, Closure<?>> queryParamComposers) {
    this(url, queryParamComposers, QueryParamListComposingType.REPEAT);
  }

  public UrlBuilder(String url, Map<Class<?>, Closure<?>> queryParamComposers, QueryParamListComposingType queryParamListComposingType) {
    this.url = url != null ? extractQueryParams(url) : "";
    this.queryParamComposers = queryParamComposers;
    this.queryParamListComposingType = queryParamListComposingType;
  }

  /**
   * Appends the given relative path or url to this builder
   * @param urlOrEndpoint a relative path or url
   * @return this
   */
  public UrlBuilder append(String urlOrEndpoint) {
    urlOrEndpoint = extractQueryParams(urlOrEndpoint);
    if (url.isEmpty()) {
      url = urlOrEndpoint;
      return this;
    }
    if (url.endsWith("/")) {
      url = url + (urlOrEndpoint.startsWith("/") ? urlOrEndpoint.substring(1) : urlOrEndpoint);
    } else {
      url = url + (urlOrEndpoint.startsWith("/") ? urlOrEndpoint :  "/" + urlOrEndpoint);
    }
    return this;
  }

  /**
   * Add query params to this URL builder
   * @param queryParams maps containing query params
   * @return this
   */
  public UrlBuilder addParams(Map<?, ?> queryParams) {
    for (Map.Entry<?, ?> entry : queryParams.entrySet()) {
      addParam(entry.getKey(), entry.getValue());
    }
    return this;
  }

  /**
   * Add a query param
   * @param key the name of the parameter
   * @param value its value
   * @return this
   */
  public UrlBuilder addParam(Object key, Object value) {
    if (value instanceof Collection) {
      return addParam(key, (Collection<?>) value);
    } else {
      queryParams.add(new QueryParam(String.valueOf(key), composeParam(value)));
    }
    return this;
  }

  public UrlBuilder addParam(Object key, Collection<?> value) {
    switch (queryParamListComposingType) {
      case COMMA:
        StringBuilder commaListBuilder = new StringBuilder();
        for (Object o : value) {
          commaListBuilder.append(composeParam(o));
        }
        queryParams.add(new QueryParam(String.valueOf(key), commaListBuilder.toString()));
        break;
      case BRACKETS:
        StringBuilder bracketsListBuilder = new StringBuilder();
        bracketsListBuilder.append("[");
        for (Object o : value) {
          bracketsListBuilder.append(composeParam(o));
        }
        bracketsListBuilder.append("]");
        queryParams.add(new QueryParam(String.valueOf(key), bracketsListBuilder.toString()));
        break;
      case REPEAT:
        for (Object o : value) {
          queryParams.add(new QueryParam(String.valueOf(key), composeParam(o)));
        }
    }
    return this;
  }

  private String composeParam(Object value) {
    if (value == null) {
      return "null";
    }
    Closure<?> closure = queryParamComposers.get(value.getClass());
    return closure != null ? String.valueOf(closure.call(value)) : String.valueOf(value);
  }

  /**
   * Returns the full URL represented by this builder, with encoded query parameters if any
   * @return the full URL represented by this builder, with encoded query parameters if any
   */
  public String encoded() {
    if (queryParams.isEmpty()) {
      return url;
    }
    return url + "?" + queryParams.stream().map(QueryParam::encoded).collect(Collectors.joining("&"));

  }

  /**
   * Returns the full URL represented by this builder, with NOT encoded query parameters if any
   * @return the full URL represented by this builder, with NOT encoded query parameters if any
   */
  @Override
  public String toString() {
    if (queryParams.isEmpty()) {
      return url;
    }
    return url + "?" + queryParams.stream().map(QueryParam::toString).collect(Collectors.joining("&"));
  }

  /**
   * Extract query param and returns the url without them
   * @param url the url
   * @return the url without query params
   */
  private String extractQueryParams(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    int start = url.indexOf("?");
    if (start < 0 || start >= url.length() - 1) {
      return url;
    }
    String paramsString = url.substring(start + 1);
    String[] params = paramsString.split("&");
    for (String param : params) {
      String[] fields = param.split("=");
      if (fields.length == 2) {
        queryParams.add(new QueryParam(urlDecode(fields[0]), urlDecode(fields[1])));
      }
    }
    return url.substring(0, start);
  }

  private String urlDecode(String s) {
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Couldn't URL decode", e);
    }
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UrlBuilder that = (UrlBuilder) o;
    return toString().equals(that.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(toString());
  }
}
