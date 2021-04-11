package com.tambapps.http.getpack;

import com.tambapps.http.getpack.auth.Auth;
import com.tambapps.http.getpack.io.Decoders;
import com.tambapps.http.getpack.io.Encoders;
import com.tambapps.http.getpack.util.UrlBuilder;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.util.XmlSlurper;
import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.codehaus.groovy.runtime.MethodClosure;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The HTTP client
 */
public class GetpackClient {

  @Getter
  private final OkHttpClient client = new OkHttpClient();
  @Getter
  private final String baseUrl;
  @Getter
  private final Map<String, String> headers = new HashMap<>();
  @Getter
  @Setter
  private Closure<?> errorResponseHandler = new MethodClosure(this, "handleErrorResponse");
  private final Map<ContentType, Closure<?>> bodyEncoders = Encoders.getMap();
  private final Map<ContentType, Closure<?>> decoders = Decoders.getMap();
  @Getter
  @Setter
  private ContentType contentType;
  @Getter
  @Setter
  private ContentType acceptContentType;
  @Getter
  @Setter
  private Auth auth;

  public GetpackClient() {
    this("");
  }

  public GetpackClient(Map<?, ?> properties) {
    this(getOrDefault(properties, "url", String.class, ""));
    Map<?, ?> headers = getOrDefault(properties, "headers", Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      putHeader(entry.getKey(), entry.getValue());
    }
    this.errorResponseHandler = getOrDefault(properties, "errorResponseHandler", Closure.class, this.errorResponseHandler);
    this.acceptContentType = getOrDefault(properties, "acceptContentType", ContentType.class, this.acceptContentType);
    this.contentType = getOrDefault(properties, "contentType", ContentType.class, null);
    this.auth = getOrDefault(properties, "auth", Auth.class, auth);
  }

  public GetpackClient(String baseUrl) {
    this.baseUrl = baseUrl != null ? baseUrl : "";
  }

  public Object method(String urlOrEndpoint, String method) throws IOException {
    return method(Collections.emptyMap(), urlOrEndpoint, method);
  }

  public Object method(Map<?, ?> additionalParameters, String urlOrEndpoint, String method) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).method(method, requestBody).build();
    return doRequest(request, additionalParameters);
  }

  public Object method(String urlOrEndpoint, String method, Closure<Void> responseHandler) throws IOException {
    return method(Collections.emptyMap(), urlOrEndpoint, method, responseHandler);
  }

  public Object method(Map<?, ?> additionalParameters, String urlOrEndpoint, String method, Closure<Void> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).method(method, requestBody).build();
    return doRequest(request, responseHandler);
  }

  public Object put(String urlOrEndpoint) throws IOException {
    return put(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object put(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).put(requestBody).build();
    return doRequest(request, additionalParameters);
  }

  public Object put(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return put(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object put(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).put(requestBody).build();
    return doRequest(request, responseHandler);
  }

  public Object patch(String urlOrEndpoint) throws IOException {
    return patch(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object patch(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).patch(requestBody).build();
    return doRequest(request, additionalParameters);
  }

  public Object patch(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object patch(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).patch(requestBody).build();
    return doRequest(request, responseHandler);
  }

  public Object post(String urlOrEndpoint) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object post(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).post(requestBody).build();
    return doRequest(request, additionalParameters);
  }

  public Object post(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object post(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).post(requestBody).build();
    return doRequest(request, responseHandler);
  }

  public Object delete(String urlOrEndpoint) throws IOException {
    return delete(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object delete(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();
    return doRequest(request, additionalParameters);
  }

  public Object delete(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return delete(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object delete(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();
    return doRequest(request, responseHandler);
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
    return doRequest(request, additionalParameters);
  }

  public Object get(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return get(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }


  /**
   * Get the following url/endpoint and use the closure as a response handler
   * @param additionalParameters additional parameters
   * @param urlOrEndpoint the url or endpoint
   * @param responseHandler the response handler
   * @return the value returned by the responseHandler
   */
  public Object get(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).get().build();
    return doRequest(request, responseHandler);
  }

  public void putHeader(Object key, Object value) {
    headers.put(String.valueOf(key), String.valueOf(value));
  }

  public boolean removeHeader(String key) {
    return headers.remove(key) != null;
  }

  private Object doRequest(Request request, Closure<Void> responseHandler) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      return responseHandler.call(response);
    }
  }

  private Object doRequest(Request request, Map<?, ?> additionalParameters) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      return handleResponse(response, additionalParameters);
    }
  }

  private Object handleResponse(Response response, Map<?, ?> additionalParameters) {
    if (!response.isSuccessful()) {
      return errorResponseHandler.call(response);
    }
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    String contentTypeHeader = response.header("Content-Type");
    ContentType responseContentType = contentTypeHeader != null ? ContentType.from(contentTypeHeader) : null;
    Closure<?> decoder = getOrDefault(additionalParameters, "decoder", Closure.class, decoders.get(responseContentType));
    if (decoder == null) {
      throw new IllegalStateException("No decoder was found for media type " + responseContentType);
    }
    return decoder.call(body);
  }

  private static <T> T getOrDefault(Map<?, ?> additionalParameters, String key, Class<T> clazz, T defaultValue) {
    Object object = additionalParameters.get(key);
    if (object == null) {
      return defaultValue;
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(String.format("Unexpected type for parameter '%s', expected type %s", key, clazz.getSimpleName()));
    }
    return (T) object;
  }

  private RequestBody requestBody(Map<?, ?> additionalParameters) {
    Object body = getOrDefault(additionalParameters, "body", Object.class, null);
    ContentType contentType = getOrDefault(additionalParameters, "contentType", ContentType.class,
        this.contentType);
    if (body == null) {
      return RequestBody.create(null, new byte[]{});
    }
    Closure<?> bodyEncoder = getOrDefault(additionalParameters, "bodyEncoder", Closure.class, bodyEncoders.get(
        contentType));
    if (bodyEncoder == null) {
      throw new IllegalStateException("No body encoder was found for media type " + contentType);
    }
    return (RequestBody) bodyEncoder.call(body, MediaType.get(contentType.toString()));
  }

  private Request.Builder request(String urlOrEndpoint, Map<?, ?> additionalParameters) {
    // url stuff
    String url = new UrlBuilder(baseUrl).append(urlOrEndpoint)
        .addParams(getOrDefault(additionalParameters, "params", Map.class, Collections.emptyMap()))
        .encoded();
    Request.Builder builder = new Request.Builder().url(url);
    // headers stuff
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }
    Map<?, ?> headers = getOrDefault(additionalParameters, "headers", Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      builder.header(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
    }
    Auth auth = getOrDefault(additionalParameters, "auth", Auth.class, this.auth);
    if (auth != null) {
      auth.apply(builder);
    }

    ContentType acceptContentType = getOrDefault(additionalParameters, "acceptContentType", ContentType.class, this.acceptContentType);
    if (acceptContentType != null) {
      builder.header("Accept", acceptContentType.toString());
    }
    return builder;
  }

  // used by method closure
  protected Object resolveContent(Response response) throws IOException {
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

  // used by method closure
  protected Object handleErrorResponse(Response response) throws IOException {
    throw new ErrorResponseException(response);
  }

}
