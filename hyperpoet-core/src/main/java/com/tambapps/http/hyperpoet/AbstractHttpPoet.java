package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.interceptor.ConsolePrintingInterceptor;
import com.tambapps.http.hyperpoet.io.IoUtils;
import com.tambapps.http.hyperpoet.io.composer.Composers;
import com.tambapps.http.hyperpoet.io.json.JsonGenerator;
import com.tambapps.http.hyperpoet.io.parser.Parsers;
import com.tambapps.http.hyperpoet.url.MultivaluedQueryParamComposingType;
import com.tambapps.http.hyperpoet.url.QueryParamComposer;
import com.tambapps.http.hyperpoet.url.UrlBuilder;
import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import com.tambapps.http.hyperpoet.util.ContentTypeMapFunction;
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
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The HTTP client
 */
@Getter
@Setter
public class AbstractHttpPoet {

  @SneakyThrows
  public static AbstractHttpPoet newPoet() {
    return (AbstractHttpPoet) Class.forName("com.tambapps.http.hyperpoet.HttpPoet").getDeclaredConstructor().newInstance();
  }
  public static final int DEFAULT_HISTORY_LIMIT = 10;

  private OkHttpClient okHttpClient;
  private final Map<String, String> headers = new HashMap<>();
  private final Map<String, Object> params = new HashMap<>();
  private final JsonGenerator jsonGenerator = new JsonGenerator();

  private final Map<Class<?>, Function<Object, ?>> queryParamConverters = new HashMap<>();
  private final QueryParamComposer queryParamComposer = new QueryParamComposer(queryParamConverters, MultivaluedQueryParamComposingType.REPEAT);
  private final ContentTypeMapFunction composers = Composers.getMap(jsonGenerator, queryParamComposer);
  private final ContentTypeMapFunction parsers = Parsers.getMap();
  private Function<Object, ?> errorResponseHandler = ErrorResponseHandlers.throwResponseHandler();
  protected Function<Object, ?> onPreExecute;
  protected Function<Object, ?> onPostExecute;
  private String baseUrl;
  private ContentType contentType;
  private ContentType acceptContentType;
  private History history;

  public AbstractHttpPoet() {
    this("");
  }

  public AbstractHttpPoet(OkHttpClient client) {
    this(client, "");
  }
  public AbstractHttpPoet(String baseUrl) {
    this(new OkHttpClient(), baseUrl);

    final DateTimeFormatter ldtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Function localDateTimeFormatter = (o) -> ldtf.format((TemporalAccessor) o);

    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    Function dateTimeFormatter = (o) -> dtf.format((TemporalAccessor) o);
    /*
    jsonGenerator.addConverter(LocalDateTime.class, localDateTimeFormatter);
    jsonGenerator.addConverter(LocalDate.class, dateTimeFormatter);
     */

    queryParamConverters.put(LocalDateTime.class, localDateTimeFormatter);
    queryParamConverters.put(LocalDate.class, dateTimeFormatter);
  }

  public AbstractHttpPoet(OkHttpClient okHttpClient, String baseUrl) {
    this.okHttpClient = okHttpClient;
    this.baseUrl = baseUrl != null ? baseUrl : "";
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

  protected Object doRequest(Request request,
                           Object requestBody, boolean shouldSkipHistory,
                           ContentType acceptContentTypeOverride, Function<Object, ?> parserOverride,
                           Function<Object, ?> responseHandler) throws IOException {
    if (onPreExecute != null) {
      onPreExecute.apply(request);
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      Response effectiveResponse = handleHistory(response, shouldSkipHistory, requestBody, acceptContentTypeOverride, parserOverride);
      if (onPostExecute != null) {
        onPostExecute.apply(effectiveResponse);
      }
      return responseHandler.apply(effectiveResponse);
    }
  }

  protected Object doRequest(Request request,
      Object requestBody, boolean shouldSkipHistory, Boolean shouldPrint, Boolean shouldPrintRequestBody, Boolean shouldPrintResponseBody,
                             ContentType acceptContentTypeOverride, Function<Object, ?> parserOverride) throws IOException {
    getInterceptors().stream()
        .filter(i -> i instanceof ConsolePrintingInterceptor)
        .map(i -> (ConsolePrintingInterceptor) i)
        .findFirst().ifPresent(printingInterceptor -> {
      printingInterceptor.setShouldPrint(shouldPrint != null ? shouldPrint : true);
      printingInterceptor.setShouldPrintRequestBody(shouldPrintRequestBody != null ? shouldPrintRequestBody : true);
      printingInterceptor.setShouldPrintResponseBody(shouldPrintResponseBody != null ? shouldPrintResponseBody : true);
    });
    if (onPreExecute != null) {
      onPreExecute.apply(request);
    }
    try (Response response = okHttpClient.newCall(request).execute()) {
      Response effectiveResponse = handleHistory(response, shouldSkipHistory, requestBody, acceptContentTypeOverride, parserOverride);
      if (onPostExecute != null) {
        onPostExecute.apply(effectiveResponse);
      }
      return handleResponse(effectiveResponse, acceptContentTypeOverride, parserOverride);
    }
  }

  protected Object handleResponse(Response response, ContentType acceptContentTypeOverride, Function parserOverride) {
    if (!response.isSuccessful()) {
      return handleErrorResponse(response);
    } else {
      return parseResponse(response, acceptContentTypeOverride, parserOverride);
    }
  }

  protected Object parseResponse(Response response, ContentType acceptContentTypeOverride, Function parserOverride) {
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    Function parser = extractResponseBodyParser(response, acceptContentTypeOverride, parserOverride);
    return parser.apply(body);
  }

  private ContentType extractResponseContentType(Response response, ContentType acceptContentTypeOverride) {
    if (acceptContentTypeOverride != null) return acceptContentTypeOverride;
    if (this.acceptContentType != null) return this.acceptContentType;
    return getResponseContentType(response);
  }

  private Function extractResponseBodyParser(Response response, ContentType acceptContentTypeOverride, Function parserOverride) {
    ContentType responseContentType = extractResponseContentType(response, acceptContentTypeOverride);
    Function parser = parserOverride != null ? parserOverride : parsers.get(responseContentType);
    if (parser != null) {
      return parser;
    } else {
      return (o) -> Parsers.parseStringResponseBody((ResponseBody) o);
    }
  }

  protected ContentType getResponseContentType(Response response) {
    String contentTypeHeader = response.header(ContentType.HEADER);
    return contentTypeHeader != null ? ContentType.valueOf(contentTypeHeader) : acceptContentType;
  }

  protected RequestBody requestBody(Object body, Function composerOverride, ContentType contentType, String method) throws IOException {
    if (body == null) {
      // request body must not be null for some methods, so we return an empty body instead
      return okhttp3.internal.http.HttpMethod.requiresRequestBody(method) ? RequestBody.create(new byte[0]) : null;
    }
    // some "smart" conversions
    Object composedBody;
    if (body instanceof File) {
      composedBody = IoUtils.getBytes((File) body);
    } else if (body instanceof Path) {
      composedBody = IoUtils.getBytes(((Path) body).toFile());
    } else if (body instanceof Reader) {
      composedBody = IoUtils.getText((Reader) body);
    } else {
      Function composer = composers.getOrDefault(contentType, composerOverride);
      if (composer == null && (!(body instanceof RequestBody)
          && !(body instanceof String)
          && !(body instanceof InputStream)
          && !(body instanceof byte[]))) {
        throw new IllegalStateException("No composer was found for content type " + contentType);
      }
      composedBody = composer != null ? composer.apply(body) : body;
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
      return RequestBody.create(IoUtils.getBytes((InputStream) object), mediaType);
    } else if (object instanceof byte[]) {
      return RequestBody.create((byte[]) object, mediaType);
    } else {
      throw new IllegalStateException(
          String.format("Couldn't transform composed data of type %s to a RequestBody."
                  + "The result must either be a String, an InputStream, a byte array or a okhttp3.RequestBody",
              object.getClass().getSimpleName()));
    }
  }

  public Request request(String method, String urlOrEndpoint, Map<?, ?> params,
                         ContentType contentTypeOverride,
                         Object body, Function composerOverride, Map<?, ?> headers,
                         ContentType acceptContentTypeOverride) throws IOException {

    ContentType contentType = contentTypeOverride != null ? contentTypeOverride : this.contentType;
    // url stuff
    String url =
        new UrlBuilder(baseUrl, queryParamComposer).append(
                urlOrEndpoint)
            .addParams(this.params)
            .addParams(params != null ? params : Collections.emptyMap())
            .build();
    RequestBody requestBody = requestBody(body, composerOverride, contentType, method);
    Request.Builder builder = new Request.Builder().url(url).method(method, requestBody);
    // headers stuff
    for (Map.Entry<String, String> entry : this.headers.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      builder.header(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
    }
    ContentType acceptContentType = acceptContentTypeOverride != null ? acceptContentTypeOverride : this.acceptContentType;

    if (acceptContentType != null) {
      builder.header("Accept", acceptContentType.toString());
    }
    if (contentType != null && body != null) {
      // only specify Content-Type header if there is a body
      builder.header(ContentType.HEADER, contentType.toString());
    }
    return builder.build();
  }

  protected Object handleErrorResponse(Response response) {
    if (errorResponseHandler != null) {
      return errorResponseHandler.apply(response);
    } else {
      return defaultHandleErrorResponse(response);
    }
  }

  // used by method closure
  @SneakyThrows
  protected Object defaultHandleErrorResponse(Response response) {
    ErrorResponseException exception = ErrorResponseException.from(response);
    response.close();
    throw exception;
  }

  public MultivaluedQueryParamComposingType getMultivaluedQueryParamComposingType() {
    return queryParamComposer.getMultivaluedQueryParamComposingType();
  }

  public void setMultivaluedQueryParamComposingType(
      MultivaluedQueryParamComposingType multivaluedQueryParamComposingType) {
    queryParamComposer.setMultivaluedQueryParamComposingType(multivaluedQueryParamComposingType);
  }


  public void addInterceptor(Interceptor interceptor) {
    this.okHttpClient = okHttpClient.newBuilder()
        .addInterceptor(interceptor)
        .build();
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

  public AbstractHttpPoet enableHistory() {
    return enableHistory(DEFAULT_HISTORY_LIMIT);
  }

  public AbstractHttpPoet enableHistory(int limit) {
    if (history == null) {
      history = new History(limit);
    } else {
      history.setLimit(limit);
    }
    return this;
  }

  public void disableHistory() {
    history = null;
  }


  public void setDefaultParser(Function<Object, ?> parser) {
    getParsers().setDefaultValue(parser);
  }


  public void configureOkHttpClient(Function<Object, ?> configurer) {
    OkHttpClient.Builder builder = okHttpClient.newBuilder();
    configurer.apply(builder);
    this.okHttpClient = builder.build();
  }

  protected Response handleHistory(Response response, boolean skipHistory, Object requestBody,
                                 ContentType acceptContentTypeOverride, Function<Object, ?> parserOverride) throws IOException {
    if (history == null || skipHistory) {
      return response;
    }
    Response cachedResponse = CachedResponseBody.newResponseWitchCachedBody(response);
    Function responseParser = extractResponseBodyParser(response, acceptContentTypeOverride, parserOverride);
    history.add(new HttpExchange(cachedResponse, requestBody, responseParser));
    return cachedResponse;
  }
}
