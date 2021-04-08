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
import java.util.Collections;
import java.util.Map;

public class GetpackClient {

  private final OkHttpClient client = new OkHttpClient();
  private final String baseUrl;
  private final Closure<?> contentResolver = new MethodClosure(this, "defaultContentResolve");

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
    Request request = request(urlOrEndpoint).get().build();
    Response response = client.newCall(request).execute();
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    if (!response.isSuccessful()) {
      throw new ErrorResponseException(response);
    }
    Closure<?> contentResolver  = getOrDefault(additionalParameters, "contentResolver", this.contentResolver);
    return contentResolver.call(response);
  }

  private <T> T getOrDefault(Map<?, ?> additionalParameters, String key, T defaultValue) {
    T object = (T) additionalParameters.get(key);
    return object != null ? object : defaultValue;
  }
  /**
   * Get the following url/endpoint and use the closure as a response handler
   * @param urlOrEndpoint the url or endpoint
   * @param closure the response handler
   */
  public void get(String urlOrEndpoint, Closure<Void> closure) {

  }

  private Request.Builder request(String urlOrEndpoint) {
    // TODO handle headers
    return new Request.Builder().url(getUrl(urlOrEndpoint));
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
