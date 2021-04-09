package com.tambapps.http.getpack.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UrlBuilder {

  private String url;
  private final List<QueryParam> queryParams = new ArrayList<>();

  public UrlBuilder(String url) {
    this.url = url != null ? url : "";
    extractQueryParams(url);
  }

  public UrlBuilder append(String urlOrEndpoint) {
    if (url.isEmpty()) {
      url = urlOrEndpoint;
    }
    if (url.endsWith("/")) {
      url = url + (urlOrEndpoint.startsWith("/") ? urlOrEndpoint.substring(1) : urlOrEndpoint);
    } else {
      url = url + (urlOrEndpoint.startsWith("/") ? urlOrEndpoint :  "/" + urlOrEndpoint);
    }
    extractQueryParams(urlOrEndpoint);
    return this;
  }

  public UrlBuilder addParams(Map<?, ?> queryParams) {
    for (Map.Entry<?, ?> entry : queryParams.entrySet()) {
      addParam(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public UrlBuilder addParam(Object key, Object value) {
    queryParams.add(new QueryParam(String.valueOf(key), String.valueOf(value)));
    return this;
  }

  public String encoded() {
    if (queryParams.isEmpty()) {
      return url;
    }
    return url + "?" + queryParams.stream().map(QueryParam::encoded).collect(Collectors.joining("&"));

  }

  @Override
  public String toString() {
    if (queryParams.isEmpty()) {
      return url;
    }
    return url + "?" + queryParams.stream().map(QueryParam::toString).collect(Collectors.joining("&"));
  }

  private void extractQueryParams(String url) {
    if (url == null || url.isEmpty()) {
      return;
    }
    int start = url.indexOf("?");
    if (start < 0 || start >= url.length() - 1) {
      return;
    }
    String paramsString = url.substring(start + 1);
    String[] params = paramsString.split("&");
    for (String param : params) {
      String[] fields = param.split("=");
      if (fields.length == 2) {
        queryParams.add(new QueryParam(fields[0], fields[1]));
      }
    }
  }

}
