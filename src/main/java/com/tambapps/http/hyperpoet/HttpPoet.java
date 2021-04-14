package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.auth.Auth;
import com.tambapps.http.hyperpoet.io.Composers;
import com.tambapps.http.hyperpoet.io.Parsers;
import com.tambapps.http.hyperpoet.util.UrlBuilder;
import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The HTTP client
 */
@Getter
@Setter
public class HttpPoet {

  private final OkHttpClient okHttpClient;
  private final Map<String, String> headers = new HashMap<>();
  private final Map<ContentType, Closure<?>> composers = Composers.getMap();
  private final Map<ContentType, Closure<?>> parsers = Parsers.getMap();
  private Closure<?> errorResponseHandler = new MethodClosure(this, "handleErrorResponse");
  private Closure<?> onPreExecute = null;
  private Closure<?> onPostExecute = null;
  private String baseUrl;
  private ContentType contentType;
  private Auth auth;

  public HttpPoet() {
    this("");
  }

  public HttpPoet(OkHttpClient client) {
    this(client, "");
  }

  public HttpPoet(Map<?, ?> properties) {
    this(getOrDefaultSupply(properties, "okHttpClient", OkHttpClient.class, OkHttpClient::new),
        getOrDefault(properties, "url", String.class, ""));
    Map<?, ?> headers = getOrDefault(properties, "headers", Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      putHeader(entry.getKey(), entry.getValue());
    }
    this.errorResponseHandler = getOrDefault(properties, "errorResponseHandler", Closure.class, this.errorResponseHandler);
    ContentType acceptContentType = getOrDefault(properties, "acceptContentType", ContentType.class, null);
    if (acceptContentType != null) {
      acceptContentType(acceptContentType);
    }
    this.contentType = getOrDefault(properties, "contentType", ContentType.class, null);
    this.auth = getOrDefault(properties, "auth", Auth.class, auth);
  }
  public HttpPoet(String baseUrl) {
    this(new OkHttpClient(), baseUrl);
  }

  public HttpPoet(OkHttpClient okHttpClient, String baseUrl) {
    this.okHttpClient = okHttpClient;
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

  public Object method(String urlOrEndpoint, String method, Closure<?> responseHandler) throws IOException {
    return method(Collections.emptyMap(), urlOrEndpoint, method, responseHandler);
  }

  public Object method(Map<?, ?> additionalParameters, String urlOrEndpoint, String method, Closure<?> responseHandler)
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

  public Object put(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return put(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object put(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler)
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

  public Object patch(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object patch(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler)
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

  public Object post(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object post(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler)
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

  public Object delete(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return delete(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object delete(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler)
      throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();
    return doRequest(request, responseHandler);
  }

  public Object get(String urlOrEndpoint) throws IOException {
    return get(Collections.emptyMap(), urlOrEndpoint);
  }

  /**
   * Get the following url/endpoint and returns the decoded response
   * @param additionalParameters additional parameters
   * @param urlOrEndpoint the url or endpoint
   * @return the response data
   */
  public Object get(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).get().build();
    return doRequest(request, additionalParameters);
  }

  public Object get(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return get(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }


  /**
   * Get the following url/endpoint and use the closure as a response handler
   * @param additionalParameters additional parameters
   * @param urlOrEndpoint the url or endpoint
   * @param responseHandler the response handler
   * @return the value returned by the responseHandler
   */
  public Object get(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler)
      throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).get().build();
    return doRequest(request, responseHandler);
  }

  public void putHeader(Object key, Object value) {
    headers.put(String.valueOf(key), String.valueOf(value));
  }

  public void acceptContentType(ContentType contentType) {
    headers.put("Accept", contentType.toString());
  }

  public boolean removeHeader(String key) {
    return headers.remove(key) != null;
  }

  private Object doRequest(Request request, Closure<?> responseHandler) throws IOException {
    if (onPreExecute != null) {
      onPreExecute.call(request);
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      if (onPostExecute != null) {
        onPostExecute.call(request, response);
      }
      return responseHandler.call(response);
    }
  }

  private Object doRequest(Request request, Map<?, ?> additionalParameters) throws IOException {
    if (onPreExecute != null) {
      onPreExecute.call(request);
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      if (onPostExecute != null) {
        onPostExecute.call(request, response);
      }
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
    Closure<?> parser = getOrDefault(additionalParameters, "parser", Closure.class, parsers.get(responseContentType));
    if (parser == null) {
      throw new IllegalStateException("No parser was found for content type " + responseContentType);
    }
    return parser.call(body);
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

  private static <T> T getOrDefaultSupply(Map<?, ?> additionalParameters, String key, Class<T> clazz, Supplier<T> defaultValueSupplier) {
    Object object = additionalParameters.get(key);
    if (object == null) {
      return defaultValueSupplier.get();
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(String.format("Unexpected type for parameter '%s', expected type %s", key, clazz.getSimpleName()));
    }
    return (T) object;
  }

  private RequestBody requestBody(Map<?, ?> additionalParameters) throws IOException {
    Object body = getOrDefault(additionalParameters, "body", Object.class, null);
    ContentType contentType = getOrDefault(additionalParameters, "contentType", ContentType.class,
        this.contentType);
    if (body == null) {
      return RequestBody.create(null, new byte[]{});
    }
    Closure<?> composer = getOrDefault(additionalParameters, "composer", Closure.class,
        composers.get(contentType));
    if (composer == null) {
      throw new IllegalStateException("No composer was found for content type " + contentType);
    }
    MediaType mediaType = contentType != null ? MediaType.get(contentType.toString()) : null;
    return toRequestBody(composer.call(body), mediaType);
  }

  private RequestBody toRequestBody(Object object, MediaType mediaType) throws IOException {
    if (object instanceof RequestBody) {
      return (RequestBody) object;
    } else if (object instanceof String) {
      return RequestBody.create(object.toString().getBytes(StandardCharsets.UTF_8), mediaType);
    } else if (object instanceof InputStream) {
      return RequestBody.create(IOGroovyMethods.getBytes((InputStream) object), mediaType);
    } else if (object instanceof byte[]) {
      return RequestBody.create((byte[]) object, mediaType);
    } else {
      throw new IllegalStateException(String.format("Couldn't transform composed data of type %s to a RequestBody."
              + "The result must either be a String, an InputStream, a byte array or a okhttp3.RequestBody",
          object.getClass().getSimpleName()));
    }
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
    ContentType acceptContentType = getOrDefault(additionalParameters, "acceptContentType", ContentType.class, null);
    if (acceptContentType != null) {
      builder.header("Accept", contentType.toString());
    }
    // auth stuff
    Auth auth = getOrDefault(additionalParameters, "auth", Auth.class, this.auth);
    if (auth != null) {
      auth.apply(builder);
    }
    return builder;
  }

  // used by method closure
  protected Object handleErrorResponse(Response response) throws IOException {
    throw new ErrorResponseException(response);
  }

}
