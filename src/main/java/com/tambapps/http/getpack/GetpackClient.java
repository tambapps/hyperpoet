package com.tambapps.http.getpack;

import com.tambapps.http.getpack.exception.ErrorResponseException;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.util.XmlSlurper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.codehaus.groovy.runtime.MethodClosure;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetpackClient {

  private final OkHttpClient client = new OkHttpClient();
  private final String baseUrl;
  private final Closure<?> contentResolver = new MethodClosure(this, "defaultContentResolve");
  private final Map<String, String> defaultHeaders = new HashMap<>();

  public GetpackClient() {
    this("");
  }

  public GetpackClient(String baseUrl) {
    this.baseUrl = baseUrl != null ? baseUrl : "";
  }

  public Object get(String urlOrEndpoint) throws IOException {
    return get(Collections.emptyMap(), urlOrEndpoint);
  }

  /**
   * Get the following url/endpoint and returns the response
   * @param additionalParameters additional parameters
   * @param urlOrEndpoint the url or endpoint
   * @return the response data
   */
  public Object get(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).get().build();

    Response response = client.newCall(request).execute();
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    if (!response.isSuccessful()) {
      throw new ErrorResponseException(response);
    }
    Closure<?> contentResolver  = getOrDefault(additionalParameters, "contentResolver", Closure.class, this.contentResolver);
    return contentResolver.call(response);
  }

  private <T> T getOrDefault(Map<?, ?> additionalParameters, String key, Class<T> clazz, T defaultValue) {
    Object object = additionalParameters.get(key);
    if (object == null) {
      return defaultValue;
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(String.format("Unexpected type for parameter '%s'", key));
    }
    return (T) object;
  }

  /**
   * Get the following url/endpoint and use the closure as a response handler
   * @param urlOrEndpoint the url or endpoint
   * @param closure the response handler
   */
  public void get(String urlOrEndpoint, Closure<Void> closure) {

  }

  public Map<String, String> getDefaultHeaders() {
    return defaultHeaders;
  }

  public void putDefaultHeader(String key, String value) {
    defaultHeaders.put(key, value);
  }

  public boolean removeDefaultHeader(String key) {
    return defaultHeaders.remove(key) != null;
  }

  private Request.Builder request(String urlOrEndpoint, Map<?, ?> additionalParameters) {
    // url stuff
    String url = getUrl(urlOrEndpoint);
    Map<?, ?> queryParams = getOrDefault(additionalParameters, "query", Map.class, Collections.emptyMap());
    List<String> params = new ArrayList<>();
    for (Map.Entry<?, ?> entry : queryParams.entrySet()) {
      params.add(String.format("%s=%s", urlEncode(entry.getKey()), urlEncode(entry.getValue())));
    }
    if (!params.isEmpty()) {
      url = url + "?" + String.join("&", params);
    }
    Request.Builder builder = new Request.Builder().url(url);
    // headers stuff
    for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }
    Map<?, ?> headers = getOrDefault(additionalParameters, "headers", Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      builder.header(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
    }
    return builder;
  }

  private String urlEncode(Object o) {
    try {
      return URLEncoder.encode(String.valueOf(o), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Couldn't URL encode", e);
    }
  }

  private String getUrl(String urlOrEndpoint) {
    if (baseUrl.isEmpty()) {
      return urlOrEndpoint;
    }
    if (baseUrl.endsWith("/")) {
      return baseUrl + (urlOrEndpoint.startsWith("/") ? urlOrEndpoint.substring(1) : urlOrEndpoint);
    } else {
      return baseUrl + (urlOrEndpoint.startsWith("/") ? urlOrEndpoint :  "/" + urlOrEndpoint);
    }
  }

  // used by method closure
  protected Object defaultContentResolve(Response response) throws IOException {
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    String responseContentType = response.headers().get("Content-Type");
    if (responseContentType == null) {
      return body.string();
    } else if (responseContentType.contains("application/json")) {
      return new JsonSlurper().parseText(body.string());
    } else if (responseContentType.contains("application/xml")) {
      try {
        return new XmlSlurper().parseText(body.string());
      } catch (SAXException | ParserConfigurationException e) {
        throw new IOException("Error while parsing XML", e);
      }
    } else if (responseContentType.contains("application/octet-stream")) {
      return body.bytes();
    } else {
      return body.string();
    }
  }
}
