package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.auth.Auth;
import com.tambapps.http.hyperpoet.io.Composers;
import com.tambapps.http.hyperpoet.io.Parsers;
import com.tambapps.http.hyperpoet.io.PoeticJsonGenerator;
import com.tambapps.http.hyperpoet.util.QueryParamComposer;
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
import okio.BufferedSink;
import okio.Okio;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  protected final OkHttpClient okHttpClient;
  private final Map<String, String> headers = new HashMap<>();
  private final PoeticJsonGenerator jsonGenerator = new PoeticJsonGenerator();
  private final Map<Class<?>, Closure<?>> queryParamConverters = new HashMap<>();
  // TODO document it
  private UrlBuilder.MultivaluedQueryParamComposingType multivaluedQueryParamComposingType =
      UrlBuilder.MultivaluedQueryParamComposingType.REPEAT;
  private final QueryParamComposer queryParamComposer = new QueryParamComposer(queryParamConverters, multivaluedQueryParamComposingType);
  private final Map<ContentType, Closure<?>> composers = Composers.getMap(jsonGenerator, queryParamComposer);
  private final Map<ContentType, Closure<?>> parsers = Parsers.getMap();
  // TODO document it
  private Closure<?> errorResponseHandler = new MethodClosure(this, "handleErrorResponse");
  protected Closure<?> onPreExecute;
  protected Closure<?> onPostExecute;
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
    this.errorResponseHandler =
        getOrDefault(properties, "errorResponseHandler", Closure.class, this.errorResponseHandler);
    this.onPreExecute = getOrDefault(properties, "onPreExecute", Closure.class, null);
    this.onPostExecute = getOrDefault(properties, "onPostExecute", Closure.class, null);
    ContentType acceptContentType =
        getOrDefault(properties, "acceptContentType", ContentType.class, null);
    if (acceptContentType != null) {
      acceptContentType(acceptContentType);
    }
    this.contentType = getOrDefault(properties, "contentType", ContentType.class, null);
    this.auth = getOrDefault(properties, "auth", Auth.class, auth);

    Closure<?> localDateTimeFormatter = new MethodClosure(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"), "format");
    Closure<?> dateTimeFormatter = new MethodClosure(DateTimeFormatter.ofPattern("yyyy-MM-dd"), "format");
    jsonGenerator.addConverter(LocalDateTime.class, localDateTimeFormatter);
    jsonGenerator.addConverter(LocalDate.class, dateTimeFormatter);
    queryParamConverters.put(LocalDateTime.class, localDateTimeFormatter);
    queryParamConverters.put(LocalDate.class, dateTimeFormatter);
  }

  public HttpPoet(String baseUrl) {
    this(new OkHttpClient(), baseUrl);
  }

  public HttpPoet(OkHttpClient okHttpClient, String baseUrl) {
    this.okHttpClient = okHttpClient;
    this.baseUrl = baseUrl != null ? baseUrl : "";
  }

  /**
   * Performs a request with the given method name and return the parsed result
   *
   * @param urlOrEndpoint the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(String urlOrEndpoint, String method) throws IOException {
    return method(Collections.emptyMap(), urlOrEndpoint, method);
  }

  /**
   * Performs a request with the provided method name and additional parameters and return the parsed result
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(Map<?, ?> additionalParameters, String urlOrEndpoint, String method)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request =
        request(urlOrEndpoint, additionalParameters).method(method, requestBody).build();
    return doRequest(request, additionalParameters);
  }

  /**
   * Performs a request with the provided method name and response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(String urlOrEndpoint, String method, Closure<?> responseHandler)
      throws IOException {
    return method(Collections.emptyMap(), urlOrEndpoint, method, responseHandler);
  }

  /**
   * Performs a request with the provided method name, response handler and additional parameters
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param responseHandler      the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(Map<?, ?> additionalParameters, String urlOrEndpoint, String method,
      Closure<?> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request =
        request(urlOrEndpoint, additionalParameters).method(method, requestBody).build();
    return doRequest(request, responseHandler);
  }

  /**
   * Performs a put request and return the parsed result
   *
   * @param urlOrEndpoint the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object put(String urlOrEndpoint) throws IOException {
    return put(Collections.emptyMap(), urlOrEndpoint);
  }

  /**
   * Performs a put request with the provided additional parameters and return the parsed result
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object put(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).put(requestBody).build();
    return doRequest(request, additionalParameters);
  }

  /**
   * Performs a put request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object put(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return put(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }


  /**
   * Performs a put request with the provided response handler and the provided additional parameters
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param responseHandler      the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object put(Map<?, ?> additionalParameters, String urlOrEndpoint,
      Closure<?> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).put(requestBody).build();
    return doRequest(request, responseHandler);
  }

  /**
   * Performs a patch request and return the parsed result
   *
   * @param urlOrEndpoint the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object patch(String urlOrEndpoint) throws IOException {
    return patch(Collections.emptyMap(), urlOrEndpoint);
  }

  /**
   * Performs a patch request with the provided additional parameters and return the parsed result
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object patch(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).patch(requestBody).build();
    return doRequest(request, additionalParameters);
  }

  /**
   * Performs a patch request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object patch(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  /**
   * Performs a patch request with the provided response handler and the provided additional parameters
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param responseHandler      the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object patch(Map<?, ?> additionalParameters, String urlOrEndpoint,
      Closure<?> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).patch(requestBody).build();
    return doRequest(request, responseHandler);
  }

  /**
   * Performs a post request and return the parsed result
   *
   * @param urlOrEndpoint the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object post(String urlOrEndpoint) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint);
  }

  /**
   * Performs a post request with the provided additional parameters and return the parsed result
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object post(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).post(requestBody).build();
    return doRequest(request, additionalParameters);
  }

  /**
   * Performs a post request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object post(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  /**
   * Performs a post request with the provided response handler and the provided additional parameters
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param responseHandler      the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object post(Map<?, ?> additionalParameters, String urlOrEndpoint,
      Closure<?> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).post(requestBody).build();
    return doRequest(request, responseHandler);
  }

  /**
   * Performs a delete request and return the parsed result
   *
   * @param urlOrEndpoint the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object delete(String urlOrEndpoint) throws IOException {
    return delete(Collections.emptyMap(), urlOrEndpoint);
  }

  /**
   * Performs a delete request with the provided additional parameters and return the parsed result
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object delete(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();
    return doRequest(request, additionalParameters);
  }

  /**
   * Performs a delete request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object delete(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return delete(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  /**
   * Performs a delete request with the provided response handler and the provided additional parameters
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param responseHandler      the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object delete(Map<?, ?> additionalParameters, String urlOrEndpoint,
      Closure<?> responseHandler)
      throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();
    return doRequest(request, responseHandler);
  }

  /**
   * Performs a get request and return the parsed result
   *
   * @param urlOrEndpoint the url or endpoint
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object get(String urlOrEndpoint) throws IOException {
    return get(Collections.emptyMap(), urlOrEndpoint);
  }

  /**
   * Get the following url/endpoint and returns the decoded response
   *
   * @param additionalParameters additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @return the response data
   */
  public Object get(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).get().build();
    return doRequest(request, additionalParameters);
  }

  /**
   * Performs a get request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object get(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    return get(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }


  /**
   * Get the following url/endpoint and use the closure as a response handler
   *
   * @param additionalParameters additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param responseHandler      the response handler
   * @return the value returned by the responseHandler
   */
  public Object get(Map<?, ?> additionalParameters, String urlOrEndpoint,
      Closure<?> responseHandler)
      throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).get().build();
    return doRequest(request, responseHandler);
  }

  /**
   * Add/Replace the given header with the given value
   *
   * @param key   the header name
   * @param value the header value
   */
  public void putHeader(Object key, Object value) {
    headers.put(String.valueOf(key), String.valueOf(value));
  }

  /**
   * Set the Content-Type header
   *
   * @param contentType the content type
   */
  public void acceptContentType(ContentType contentType) {
    headers.put("Accept", contentType.toString());
  }

  /**
   * Removes a header
   *
   * @param key the name of the header
   * @return the removed header or null if there wern't any
   */
  public String removeHeader(String key) {
    return headers.remove(key);
  }

  private Object doRequest(Request request, Closure<?> responseHandler) throws IOException {
    if (onPreExecute != null) {
      // TODO document it
      if (onPreExecute.getMaximumNumberOfParameters() > 1) {
        onPreExecute.call(request, extractRequestBody(request.body()));
      } else {
        onPreExecute.call(request);
      }
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      if (onPostExecute != null) {
        onPostExecute.call(response);
      }
      return responseHandler.call(response);
    }
  }

  private Object doRequest(Request request, Map<?, ?> additionalParameters) throws IOException {
    if (onPreExecute != null) {
      if (onPreExecute.getMaximumNumberOfParameters() > 1) {
        onPreExecute.call(request, extractRequestBody(request.body()));
      } else {
        onPreExecute.call(request);
      }
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      if (onPostExecute != null) {
        onPostExecute.call(response);
      }
      return handleResponse(response, additionalParameters);
    }
  }

  private Object handleResponse(Response response, Map<?, ?> additionalParameters) {
    if (!response.isSuccessful()) {
      return errorResponseHandler.call(response);
    }
    return parseResponseBody(response, additionalParameters);
  }

  protected Object parseResponseBody(Response response, Map<?, ?> additionalParameters) {
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    String contentTypeHeader = response.header("Content-Type");
    ContentType responseContentType =
        contentTypeHeader != null ? ContentType.from(contentTypeHeader) : null;
    Closure<?> parser = getOrDefault(additionalParameters, "parser", Closure.class,
        parsers.get(responseContentType));
    if (parser == null) {
      throw new IllegalStateException(
          "No parser was found for content type " + responseContentType);
    }
    return parser.call(body);
  }

  protected static <T> T getOrDefault(Map<?, ?> additionalParameters, String key, Class<T> clazz,
      T defaultValue) {
    Object object = additionalParameters.get(key);
    if (object == null) {
      return defaultValue;
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(
          String.format("Unexpected type for parameter '%s', expected type %s", key,
              clazz.getSimpleName()));
    }
    return (T) object;
  }

  private static <T> T getOrDefaultSupply(Map<?, ?> additionalParameters, String key,
      Class<T> clazz, Supplier<T> defaultValueSupplier) {
    Object object = additionalParameters.get(key);
    if (object == null) {
      return defaultValueSupplier.get();
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(
          String.format("Unexpected type for parameter '%s', expected type %s", key,
              clazz.getSimpleName()));
    }
    return (T) object;
  }

  protected RequestBody requestBody(Map<?, ?> additionalParameters) throws IOException {
    Object body = getOrDefault(additionalParameters, "body", Object.class, null);
    ContentType contentType = getOrDefault(additionalParameters, "contentType", ContentType.class,
        this.contentType);
    if (body == null) {
      return null;
    }
    // some "smart" conversions
    if (body instanceof File) {
      body = ResourceGroovyMethods.getText((File) body);
    } else if (body instanceof Path) {
      body = ResourceGroovyMethods.getText(((Path) body).toFile());
    } else if (body instanceof InputStream) {
      body = IOGroovyMethods.getText((InputStream) body);
    } else if (body instanceof Reader) {
      body = IOGroovyMethods.getText((Reader) body);
    }
    Closure<?> composer = getOrDefault(additionalParameters, "composer", Closure.class,
        composers.get(contentType));
    if (composer == null) {
      throw new IllegalStateException("No composer was found for content type " + contentType);
    }
    MediaType mediaType = contentType != null ? MediaType.get(contentType.toString()) : null;
    return toRequestBody(composer.call(body), mediaType);
  }

  protected byte[] extractRequestBody(RequestBody requestBody) {
    if (requestBody == null || requestBody.isOneShot()) {
      return null;
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (BufferedSink bufferedSink = Okio.buffer(Okio.sink(outputStream))) {
      requestBody.writeTo(bufferedSink);
      bufferedSink.flush();
      return outputStream.toByteArray();
    } catch (IOException e) {
      return null;
    }
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
      throw new IllegalStateException(
          String.format("Couldn't transform composed data of type %s to a RequestBody."
                  + "The result must either be a String, an InputStream, a byte array or a okhttp3.RequestBody",
              object.getClass().getSimpleName()));
    }
  }

  protected Request.Builder request(String urlOrEndpoint, Map<?, ?> additionalParameters) {
    // url stuff
    String url =
        new UrlBuilder(baseUrl, queryParamComposer).append(
                urlOrEndpoint)
            .addParams(
                getOrDefault(additionalParameters, "params", Map.class, Collections.emptyMap()))
            .build();
    Request.Builder builder = new Request.Builder().url(url);
    // headers stuff
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }
    Map<?, ?> headers =
        getOrDefault(additionalParameters, "headers", Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      builder.header(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
    }
    ContentType acceptContentType =
        getOrDefault(additionalParameters, "acceptContentType", ContentType.class, null);
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
