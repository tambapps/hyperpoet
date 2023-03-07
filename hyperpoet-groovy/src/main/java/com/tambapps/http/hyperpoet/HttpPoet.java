package com.tambapps.http.hyperpoet;

import static com.tambapps.http.hyperpoet.util.Constants.ACCEPT_CONTENT_TYPE_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.BODY_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.COMPOSER_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.CONTENT_TYPE_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.ERROR_RESPONSE_HANDLER_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.HEADER_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.OKHTTP_CLIENT_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.ON_POST_EXECUTE_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.ON_PRE_EXECUTE_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.PARSER_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.PRINT_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.PRINT_REQUEST_BODY_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.PRINT_RESPONSE_BODY_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.QUERY_PARAMS_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.SKIP_HISTORY_PARAM;
import static com.tambapps.http.hyperpoet.util.Constants.URL_PARAM;
import static com.tambapps.http.hyperpoet.util.ParametersUtils.getOrDefault;
import static com.tambapps.http.hyperpoet.util.ParametersUtils.getOrDefaultSupply;
import static com.tambapps.http.hyperpoet.util.ParametersUtils.getStringOrDefault;

import com.tambapps.http.hyperpoet.invoke.PoeticInvoker;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.transform.NamedParam;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.Transient;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * The HTTP client
 */
@Getter
@Setter
public class HttpPoet extends AbstractHttpPoet implements GroovyObject {

  public static final int DEFAULT_HISTORY_LIMIT = 10;

  private transient MetaClass metaClass = getDefaultMetaClass();
  private PoeticInvoker poeticInvoker = null;

  public HttpPoet() {
    this("");
  }

  public HttpPoet(OkHttpClient client) {
    this(client, "");
  }

  public HttpPoet(
      @NamedParam(value = OKHTTP_CLIENT_PARAM, type = OkHttpClient.class)
      @NamedParam(value = URL_PARAM, type = String.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ERROR_RESPONSE_HANDLER_PARAM, type = Closure.class)
      @NamedParam(value = ON_PRE_EXECUTE_PARAM, type = Closure.class)
      @NamedParam(value = ON_POST_EXECUTE_PARAM, type = Closure.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = Closure.class)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = Closure.class)
      Map<?, ?> properties) {
    this(getOrDefaultSupply(properties, OKHTTP_CLIENT_PARAM, OkHttpClient.class, OkHttpClient::new),
        getStringOrDefault(properties, URL_PARAM, ""));
    Map<?, ?> headers = getOrDefault(properties, HEADER_PARAM, Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      putHeader(entry.getKey(), entry.getValue());
    }
    setErrorResponseHandler(getFunctionOrDefault(properties, ERROR_RESPONSE_HANDLER_PARAM, getErrorResponseHandler()));
    this.onPreExecute = getFunctionOrDefault(properties, ON_PRE_EXECUTE_PARAM, null);
    this.onPostExecute = getFunctionOrDefault(properties, ON_POST_EXECUTE_PARAM, null);
    setAcceptContentType(getOrDefault(properties, ACCEPT_CONTENT_TYPE_PARAM, ContentType.class, null));
    setContentType(getOrDefault(properties, CONTENT_TYPE_PARAM, ContentType.class, null));
  }

  public HttpPoet(String baseUrl) {
    this(new OkHttpClient(), baseUrl);
  }

  public HttpPoet(OkHttpClient okHttpClient, String baseUrl) {
    super(okHttpClient, baseUrl);
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
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
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint, HttpMethod method,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint, String method,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
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
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler) throws IOException {
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
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
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler) throws IOException {
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
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
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler) throws IOException {
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
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
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler) throws IOException {
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
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
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler) throws IOException {
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
      @NamedParam(value = BODY_PARAM)
      @NamedParam(value = CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = COMPOSER_PARAM, type = Closure.class)
      @NamedParam(value = PARSER_PARAM, type = Closure.class)
      @NamedParam(value = QUERY_PARAMS_PARAM, type = Map.class)
      @NamedParam(value = HEADER_PARAM, type = Map.class)
      @NamedParam(value = ACCEPT_CONTENT_TYPE_PARAM, type = ContentType.class)
      @NamedParam(value = SKIP_HISTORY_PARAM, type = Boolean.class)
      Map<?, ?> additionalParameters, String urlOrEndpoint,
      @ClosureParams(value = SimpleType.class, options = "okhttp3.Response") Function responseHandler)
      throws IOException {
    Request request = request(HttpMethod.GET, urlOrEndpoint, additionalParameters);
    return doRequest(request, additionalParameters, responseHandler);
  }

  private Object doRequest(Request request, Map<?, ?> additionalParameters, Function responseHandler) throws IOException {
    if (onPreExecute != null) {
      onPreExecute.call(request);
    }
    try (Response response = getOkHttpClient().newCall(request).execute()) {
      Response effectiveResponse = handleHistory(response, additionalParameters);
      if (onPostExecute != null) {
        onPostExecute.call(effectiveResponse);
      }
      return responseHandler.call(effectiveResponse);
    }
  }

  protected Object doRequest(Request request,
      Map<?, ?> additionalParameters) throws IOException {
    ContentType acceptContentType = getOrDefault(additionalParameters, ACCEPT_CONTENT_TYPE_PARAM, ContentType.class, this.getAcceptContentType());
    return super.doRequest(request,
        getOrDefault(additionalParameters, BODY_PARAM, Object.class, null),
        getOrDefault(additionalParameters, SKIP_HISTORY_PARAM, Boolean.class, false),
        getOrDefault(additionalParameters, PRINT_PARAM, Boolean.class, true),
        getOrDefault(additionalParameters, PRINT_REQUEST_BODY_PARAM, Boolean.class, true),
        getOrDefault(additionalParameters, PRINT_RESPONSE_BODY_PARAM, Boolean.class, true),
        getOrDefault(additionalParameters, ACCEPT_CONTENT_TYPE_PARAM, ContentType.class, acceptContentType),
        getFunctionOrDefault(additionalParameters, PARSER_PARAM, null)
    );
  }

  private Function getFunctionOrDefault(Map<?, ?> additionalParameters, String key,
                                        Function defaultValue) {
    if (additionalParameters.get(key) instanceof Closure) {
      Closure<?> closure = (Closure<?>) additionalParameters.get(key);
      return closure::call;
    }
    return getOrDefault(additionalParameters, key, Function.class, defaultValue);
  }

  protected Object handleResponse(Response response, Map<?, ?> additionalParameters) {
    if (!response.isSuccessful()) {
      return handleErrorResponse(response, additionalParameters);
    } else {
      return parseResponse(response, additionalParameters);
    }
  }

  protected Object parseResponse(Response response, Map<?, ?> additionalParameters) {
    ContentType responseContentType = extractResponseContentType(response, additionalParameters);
    return super.parseResponse(response, responseContentType,
        getFunctionOrDefault(additionalParameters, PARSER_PARAM, null));
  }

  private ContentType extractResponseContentType(Response response, Map<?, ?> additionalParameters) {
    return getOrDefaultSupply(additionalParameters, ACCEPT_CONTENT_TYPE_PARAM, ContentType.class, () -> getResponseContentType(response));
  }
  public Request request(HttpMethod method, String urlOrEndpoint, Map<?, ?> additionalParameters) throws IOException {
    return request(method.name(), urlOrEndpoint, additionalParameters);
  }

  public Request request(String method, String urlOrEndpoint, Map<?, ?> additionalParameters) throws IOException {
    return super.request(method, urlOrEndpoint,
        getOrDefault(additionalParameters, QUERY_PARAMS_PARAM, Map.class, null),
        getOrDefault(additionalParameters, CONTENT_TYPE_PARAM, ContentType.class, this.getContentType()),
        getOrDefault(additionalParameters, BODY_PARAM, Object.class, null),
        getFunctionOrDefault(additionalParameters, COMPOSER_PARAM, null),
        getOrDefault(additionalParameters, HEADER_PARAM, Map.class, Collections.emptyMap()),
        getOrDefault(additionalParameters, ACCEPT_CONTENT_TYPE_PARAM, ContentType.class, this.getAcceptContentType())
        );
  }

  protected Object handleErrorResponse(Response response, Map<?, ?> additionalParameters) {
    if (getErrorResponseHandler() != null) {
      return getErrorResponseHandler().call(response);
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
      return this.getMetaClass().invokeMethod(this, name, args);
    } catch (MissingMethodException e) {
      if (getPoeticInvoker() == null) {
        throw e;
      }
      return getPoeticInvoker().invokeOrThrow(this, name, (args instanceof Object[]) ? (Object[]) args : new Object[] {args}, e);
    }
  }

  public void addInterceptor(@ClosureParams(value = SimpleType.class, options = "okhttp3.Interceptor.Chain") Closure<Response> interceptor) {
    addInterceptor(interceptor::call);
  }

  public void addNetworkInterceptor(@ClosureParams(value = SimpleType.class, options = "okhttp3.Interceptor.Chain") Closure<Response> interceptor) {
    addNetworkInterceptor(interceptor::call);
  }

  public HttpPoem poem() {
    return new HttpPoem(this);
  }

  public Object poem(@DelegatesTo(HttpPoem.class) Closure closure) {
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
    boolean skipHistory = getOrDefault(additionalParameters, SKIP_HISTORY_PARAM, Boolean.class, false);
    Object requestBody = getOrDefault(additionalParameters, BODY_PARAM, Object.class, null);
    ContentType acceptContentType = extractResponseContentType(response, additionalParameters);
    Function parser = getFunctionOrDefault(additionalParameters, PARSER_PARAM, null);
    return super.handleHistory(response, skipHistory, requestBody, acceptContentType, parser);
  }


  @Override
  protected RequestBody requestBody(Object body, Function composerOverride, ContentType contentType, String method) throws IOException {
    if (body instanceof Closure) {
      body = ((Closure) body).call();
    }
    return super.requestBody(body, composerOverride, contentType, method);
  }

  @Transient
  public MetaClass getMetaClass() {
    return this.metaClass;
  }

  public void setMetaClass(MetaClass metaClass) {
    this.metaClass = (MetaClass) Optional.ofNullable(metaClass).orElseGet(this::getDefaultMetaClass);
  }
  private MetaClass getDefaultMetaClass() {
    return InvokerHelper.getMetaClass(this.getClass());
  }

}
