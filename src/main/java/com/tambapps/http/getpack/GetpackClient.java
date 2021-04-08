package com.tambapps.http.getpack;

import groovy.lang.Closure;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Map;

public class GetpackClient {

  private final OkHttpClient client = new OkHttpClient();
  private final String baseUrl;

  public GetpackClient() {
    this("");
  }

  public GetpackClient(String baseUrl) {
    this.baseUrl = baseUrl != null ? baseUrl : "";
  }

  /**
   * Get the following url/endpoint and returns the response
   * @param urlOrEndpoint the url or endpoint
   * @param additionalParameters additional parameters
   * @return
   */
  public Object get(String urlOrEndpoint, Map<?, ?> additionalParameters) throws IOException {
    Request request = request(urlOrEndpoint).get().build();
    Response response = client.newCall(request).execute();
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    // TODO parse body in function of response headers
    //  and then in function of a content-type chooser
    return body.string();
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
}
