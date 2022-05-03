package com.tambapps.http.hyperpoet;

import static com.tambapps.http.hyperpoet.util.ParametersUtils.getOrDefault;
import static com.tambapps.http.hyperpoet.util.ParametersUtils.getOrDefaultSupply;
import static com.tambapps.http.hyperpoet.util.ParametersUtils.getStringOrDefault;

import com.tambapps.http.hyperpoet.invoke.PoeticInvoker;
import com.tambapps.http.hyperpoet.io.composer.Composers;
import com.tambapps.http.hyperpoet.io.parser.Parsers;
import com.tambapps.http.hyperpoet.io.json.CustomJsonGenerator;
import com.tambapps.http.hyperpoet.url.MultivaluedQueryParamComposingType;
import com.tambapps.http.hyperpoet.url.QueryParamComposer;
import com.tambapps.http.hyperpoet.url.UrlBuilder;
import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import com.tambapps.http.hyperpoet.util.ContentTypeMap;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.transform.NamedParam;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.Interceptor;
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
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The HTTP client
 */
@Getter
@Setter
public class HttpPoet extends GroovyObjectSupport {

  public static final int DEFAULT_HISTORY_LIMIT = 10;

  private OkHttpClient okHttpClient;
  private final Map<String, String> headers = new HashMap<>();
  private final Map<String, Object> params = new HashMap<>();
  private final CustomJsonGenerator jsonGenerator = new CustomJsonGenerator();
  private final Map<Class<?>, Closure<?>> queryParamConverters = new HashMap<>();
  private final QueryParamComposer queryParamComposer = new QueryParamComposer(queryParamConverters, MultivaluedQueryParamComposingType.REPEAT);
  private final ContentTypeMap<Closure<?>> composers = Composers.getMap(jsonGenerator, queryParamComposer);
  private final ContentTypeMap<Closure<?>> parsers = Parsers.getMap();
  // TODO document in release note that I changed the default error response handler
  private Closure<?> errorResponseHandler = ErrorResponseHandlers.parseResponseHandler(this);
  protected Closure<?> onPreExecute;
  protected Closure<?> onPostExecute;
  private String baseUrl;
  private ContentType contentType;
  private ContentType acceptContentType;
  private PoeticInvoker poeticInvoker = null;
  private History history;

  public HttpPoet() {
    this("");
  }

  public HttpPoet(OkHttpClient client) {
    this(client, "");
  }

  public HttpPoet(
      @NamedParam(value = "okHttpClient", type = OkHttpClient.class)
      @NamedParam(value = "url", type = String.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "errorResponseHandler", type = Closure.class)
      @NamedParam(value = "onPreExecute", type = Closure.class)
      @NamedParam(value = "onPostExecute", type = Closure.class)
      @NamedParam(value = "acceptContentType", type = Closure.class)
      @NamedParam(value = "contentType", type = Closure.class)
      Map<?, ?> properties) {
    this(getOrDefaultSupply(properties, "okHttpClient", OkHttpClient.class, OkHttpClient::new),
        getStringOrDefault(properties, "url", ""));
    Map<?, ?> headers = getOrDefault(properties, "headers", Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      putHeader(entry.getKey(), entry.getValue());
    }
    this.errorResponseHandler =
        getOrDefault(properties, "errorResponseHandler", Closure.class, this.errorResponseHandler);
    this.onPreExecute = getOrDefault(properties, "onPreExecute", Closure.class, null);
    this.onPostExecute = getOrDefault(properties, "onPostExecute", Closure.class, null);
    acceptContentType =
        getOrDefault(properties, "acceptContentType", ContentType.class, null);
    this.contentType = getOrDefault(properties, "contentType", ContentType.class, null);

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

  public Object method(String urlOrEndpoint, HttpMethod method) throws IOException {
    return method(urlOrEndpoint, method.toString());
  }


  /**
   * Performs a request with the given method name and return the parsed result
   *
   * @param urlOrEndpoint the url or endpoint
   * @param method the HTTP method
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(String urlOrEndpoint, String method) throws IOException {
    return method(Collections.emptyMap(), urlOrEndpoint, method);
  }

  public Object method(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
          Map<?, ?> additionalParameters, String urlOrEndpoint, HttpMethod method)
      throws IOException {
    return method(additionalParameters, urlOrEndpoint, method.name());
  }

  /**
   * Performs a request with the provided method name and additional parameters and return the parsed result
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param method        the HTTP method
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint, String method)
      throws IOException {
    Request request = request(method, urlOrEndpoint, additionalParameters);
    return doRequest(request, additionalParameters);
  }

  /**
   * Performs a request with the provided method name and response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param method   the HTTP method
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   *
   */
  public Object method(String urlOrEndpoint, String method,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    return method(Collections.emptyMap(), urlOrEndpoint, method, responseHandler);
  }

  /**
   * Performs a request with the provided method name, response handler and additional parameters
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param method        the HTTP method
   * @param responseHandler      the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint, HttpMethod method,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    return method(additionalParameters, urlOrEndpoint, method.name(), responseHandler);
  }

  /**
   * Performs a request with the provided method name, response handler and additional parameters
   *
   * @param additionalParameters the additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param method        the HTTP method
   * @param responseHandler      the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object method(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint, String method,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    Request request = request(method, urlOrEndpoint, additionalParameters);
    return doRequest(request, additionalParameters, responseHandler);
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
  public Object put(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    return method(additionalParameters, urlOrEndpoint, HttpMethod.PUT);
  }

  /**
   * Performs a put request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object put(String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler) throws IOException {
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
  public Object put(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    return method(additionalParameters, urlOrEndpoint, HttpMethod.PUT, responseHandler);
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
  public Object patch(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    return method(additionalParameters, urlOrEndpoint, HttpMethod.PATCH);
  }

  /**
   * Performs a patch request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object patch(String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler) throws IOException {
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
  public Object patch(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    return method(additionalParameters, urlOrEndpoint, HttpMethod.PATCH, responseHandler);
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
  public Object post(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    return method(additionalParameters, urlOrEndpoint, HttpMethod.POST);
  }

  /**
   * Performs a post request with the provided response handler
   *
   * @param urlOrEndpoint   the url or endpoint
   * @param responseHandler the response handler
   * @return the result returned by the response handler
   * @throws IOException in case of I/O error
   */
  public Object post(String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler) throws IOException {
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
  public Object post(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    return method(additionalParameters, urlOrEndpoint, HttpMethod.POST, responseHandler);
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
  public Object delete(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(HttpMethod.DELETE, urlOrEndpoint, additionalParameters);
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
  public Object delete(String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler) throws IOException {
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
  public Object delete(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    Request request = request(HttpMethod.DELETE, urlOrEndpoint, additionalParameters);
    return doRequest(request, additionalParameters, responseHandler);
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
   * @throws IOException in case of I/O errors
   */
  public Object get(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(HttpMethod.GET, urlOrEndpoint, additionalParameters);
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
  public Object get(String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler) throws IOException {
    return get(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }


  /**
   * Get the following url/endpoint and use the closure as a response handler
   *
   * @param additionalParameters additional parameters
   * @param urlOrEndpoint        the url or endpoint
   * @param responseHandler      the response handler
   * @return the value returned by the responseHandler
   * @throws IOException in case of I/O errors
   */
  public Object get(
      @NamedParam(value = "body")
      @NamedParam(value = "contentType", type = ContentType.class)
      @NamedParam(value = "composer", type = Closure.class)
      @NamedParam(value = "parser", type = Closure.class)
      @NamedParam(value = "params", type = Map.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "acceptContentType", type = ContentType.class)
      @NamedParam(value = "skipHistory", type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Closure<?> responseHandler)
      throws IOException {
    Request request = request(HttpMethod.GET, urlOrEndpoint, additionalParameters);
    return doRequest(request, additionalParameters, responseHandler);
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

  public void putHeader(List<?> header) {
    if (header.size() != 2) {
      throw new IllegalArgumentException("Argument should have two elements");
    }
    putHeader(header.get(0), header.get(1));
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

  private Object doRequest(Request request, Map<?, ?> additionalParameters, Closure<?> responseHandler) throws IOException {
    if (onPreExecute != null) {
      if (onPreExecute.getMaximumNumberOfParameters() > 1) {
        onPreExecute.call(request, extractRequestBody(request.body()));
      } else {
        onPreExecute.call(request);
      }
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      Response effectiveResponse = handleHistory(response, additionalParameters);
      if (onPostExecute != null) {
        onPostExecute.call(effectiveResponse);
      }
      return responseHandler.call(effectiveResponse);
    }
  }

  protected Object doRequest(Request request,
      Map<?, ?> additionalParameters) throws IOException {
    if (onPreExecute != null) {
      if (onPreExecute.getMaximumNumberOfParameters() > 1) {
        onPreExecute.call(request, extractRequestBody(request.body()));
      } else {
        onPreExecute.call(request);
      }
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      Response effectiveResponse = handleHistory(response, additionalParameters);
      if (onPostExecute != null) {
        onPostExecute.call(effectiveResponse);
      }
      return handleResponse(effectiveResponse, additionalParameters);
    }
  }

  protected Object handleResponse(Response response, Map<?, ?> additionalParameters) {
    if (!response.isSuccessful()) {
      return handleErrorResponse(response, additionalParameters);
    } else {
      return parseResponse(response, additionalParameters);
    }
  }

  protected Object parseResponse(Response response, Map<?, ?> additionalParameters) {
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    Closure<?> parser = extractResponseBodyParser(response, additionalParameters);
    return parser.call(body);
  }

  private ContentType extractResponseContentType(Response response, Map<?, ?> additionalParameters) {
    return getOrDefaultSupply(additionalParameters, "acceptContentType", ContentType.class, () -> getResponseContentType(response));
  }

  private Closure<?> extractResponseBodyParser(Response response, Map<?, ?> additionalParameters) {
    ContentType responseContentType = extractResponseContentType(response, additionalParameters);
    Closure<?> parser = getOrDefault(additionalParameters, "parser", Closure.class,
        parsers.get(responseContentType));
    if (parser != null) {
      return parser;
    } else {
      return new MethodClosure(Parsers.class, "parseStringResponseBody");
    }
  }

  protected ContentType getResponseContentType(Response response) {
    String contentTypeHeader = response.header(ContentType.HEADER);
    return contentTypeHeader != null ? ContentType.valueOf(contentTypeHeader) : acceptContentType;
  }

  private RequestBody requestBody(Map<?, ?> additionalParameters, String method) throws IOException {
    Object body = getOrDefault(additionalParameters, "body", Object.class, null);
    ContentType contentType = getOrDefault(additionalParameters, "contentType", ContentType.class,
        this.contentType);
    if (body == null) {
      // request body must not be null for some methods, so we return an empty body instead
      return okhttp3.internal.http.HttpMethod.requiresRequestBody(method) ? RequestBody.create(new byte[0]) : null;
    }
    if (body instanceof Closure) {
      body = ((Closure) body).call();
    }
    // some "smart" conversions
    Object composedBody;
    if (body instanceof File) {
      composedBody = ResourceGroovyMethods.getBytes((File) body);
    } else if (body instanceof Path) {
      composedBody = ResourceGroovyMethods.getBytes(((Path) body).toFile());
    } else if (body instanceof Reader) {
      composedBody = IOGroovyMethods.getText((Reader) body);
    } else {
      Closure<?> composer = getOrDefault(additionalParameters, "composer", Closure.class,
          composers.get(contentType));
      if (composer == null && (!(body instanceof RequestBody)
          && !(body instanceof String)
          && !(body instanceof InputStream)
          && !(body instanceof byte[]))) {
        throw new IllegalStateException("No composer was found for content type " + contentType);
      }
      composedBody = composer != null ? composer.call(body) : body;
    }
    MediaType mediaType = contentType != null ? contentType.toMediaType() : null;
    return toRequestBody(composedBody, mediaType);
  }

  public static byte[] extractRequestBody(RequestBody requestBody) throws IOException {
    if (requestBody == null || requestBody.isOneShot()) {
      return null;
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (BufferedSink bufferedSink = Okio.buffer(Okio.sink(outputStream))) {
      requestBody.writeTo(bufferedSink);
      bufferedSink.flush();
      return outputStream.toByteArray();
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

  public Request request(HttpMethod method, String urlOrEndpoint, Map<?, ?> additionalParameters) throws IOException {
    return request(method.name(), urlOrEndpoint, additionalParameters);
  }

  public Request request(String method, String urlOrEndpoint, Map<?, ?> additionalParameters) throws IOException {
    // url stuff
    String url =
        new UrlBuilder(baseUrl, queryParamComposer).append(
                urlOrEndpoint)
            .addParams(params)
            .addParams(
                getOrDefault(additionalParameters, "params", Map.class, Collections.emptyMap()))
            .build();
    RequestBody requestBody = requestBody(additionalParameters, method);
    Request.Builder builder = new Request.Builder().url(url).method(method, requestBody);
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
        getOrDefault(additionalParameters, "acceptContentType", ContentType.class, this.acceptContentType);
    if (acceptContentType != null) {
      builder.header("Accept", acceptContentType.toString());
    }
    return builder.build();
  }

  protected Object handleErrorResponse(Response response, Map<?, ?> additionalParameters) {
    if (errorResponseHandler != null) {
      return errorResponseHandler.getMaximumNumberOfParameters() > 1 ? errorResponseHandler.call(response, additionalParameters) : errorResponseHandler.call(response);
    } else {
      return defaultHandleErrorResponse(response, additionalParameters);
    }
  }

  // used by method closure
  @SneakyThrows
  protected Object defaultHandleErrorResponse(Response response, Map<?, ?> additionalParameters) {
    ErrorResponseException exception = ErrorResponseException.from(response);
    response.close();
    throw exception;
  }

  @Override
  @SneakyThrows
  public Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    } catch (MissingMethodException e) {
      if (poeticInvoker == null) {
        throw e;
      }
      return poeticInvoker.invokeOrThrow(this, name, (args instanceof Object[]) ? (Object[]) args : new Object[] {args}, e);
    }
  }

  public MultivaluedQueryParamComposingType getMultivaluedQueryParamComposingType() {
    return queryParamComposer.getMultivaluedQueryParamComposingType();
  }

  public void setMultivaluedQueryParamComposingType(
      MultivaluedQueryParamComposingType multivaluedQueryParamComposingType) {
    queryParamComposer.setMultivaluedQueryParamComposingType(multivaluedQueryParamComposingType);
  }

  public void addInterceptor(@ClosureParams(value = SimpleType.class, options = "okhttp3.Interceptor.Chain") Closure<Response> interceptor) {
    addInterceptor(interceptor::call);
  }

  public void addInterceptor(Interceptor interceptor) {
    this.okHttpClient = okHttpClient.newBuilder()
        .addInterceptor(interceptor)
        .build();
  }

  public void addNetworkInterceptor(@ClosureParams(value = SimpleType.class, options = "okhttp3.Interceptor.Chain") Closure<Response> interceptor) {
    addNetworkInterceptor(interceptor::call);
  }

  public void addNetworkInterceptor(Interceptor interceptor) {
    this.okHttpClient = okHttpClient.newBuilder()
        .addNetworkInterceptor(interceptor)
        .build();
  }

  public List<Interceptor> getInterceptors() {
    return okHttpClient.interceptors();
  }

  public Interceptor getInterceptor() {
    List<Interceptor> interceptors = getInterceptors();
    return !interceptors.isEmpty() ? interceptors.get(0) : null;
  }

  public List<Interceptor> getNetworkInterceptors() {
    return okHttpClient.networkInterceptors();
  }

  public Interceptor getNetworkInterceptor() {
    List<Interceptor> interceptors = getNetworkInterceptors();
    return !interceptors.isEmpty() ? interceptors.get(0) : null;
  }

  public void setDefaultParser(@ClosureParams(value = SimpleType.class, options = "okhttp3.ResponseBody") Closure<?> parser) {
    parsers.setDefaultValue(parser);
  }

  public void configureOkHttpClient(@ClosureParams(value = SimpleType.class, options = "okhttp3.OkHttpClient.Builder") Closure<Void> configurer) {
    OkHttpClient.Builder builder = okHttpClient.newBuilder();
    configurer.call(builder);
    this.okHttpClient = builder.build();
  }

  public void enableHistory() {
    enableHistory(DEFAULT_HISTORY_LIMIT);
  }

  public void enableHistory(int limit) {
    if (history == null) {
      history = new History(limit);
    } else {
      history.setLimit(limit);
    }
  }

  public void disableHistory() {
    history = null;
  }

  public Object poem(@DelegatesTo(HttpPoem.class) Closure<?> closure) {
    // TODO maybe disable poetic invoker while running poem? (it might be ambiguous)
    return new HttpPoem(this).run(closure);
  }

  /**
   * Handle the history if it is enabled (== not null), in which case the response will be cached.
   * If it isn't enabled, the response given in parameters will be returned
   * @param response the response
   * @param additionalParameters additional parameters
   * @throws IOException in case of I/O error
   * @return the response, cached if needed
   */
  private Response handleHistory(Response response, Map<?, ?> additionalParameters) throws IOException {
    if (history == null || getOrDefault(additionalParameters, "skipHistory", Boolean.class, false)) {
      return response;
    }
    Response cachedResponse = CachedResponseBody.newResponseWitchCachedBody(response);
    Object requestBody = getOrDefault(additionalParameters, "body", Object.class, null);
    Closure<?> responseParser = extractResponseBodyParser(response, additionalParameters);
    history.add(new HttpExchange(cachedResponse, requestBody, responseParser));
    return cachedResponse;
  }
}
