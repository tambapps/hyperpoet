package com.tambapps.http.getpack;

import com.tambapps.http.getpack.auth.Auth;
import com.tambapps.http.getpack.exception.ErrorResponseException;
import com.tambapps.http.getpack.util.UrlBuilder;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.util.Node;
import groovy.util.XmlSlurper;
import groovy.xml.XmlUtil;
import lombok.Getter;
import lombok.Setter;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.MethodClosure;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetpackClient {

  @Getter
  private final OkHttpClient client = new OkHttpClient();
  @Getter
  private final String baseUrl;
  @Getter
  private final Map<String, String> defaultHeaders = new HashMap<>();
  @Getter
  @Setter
  private Closure<?> contentResolver = new MethodClosure(this, "defaultContentResolve");
  @Getter
  @Setter
  private ContentType contentType;
  @Getter
  @Setter
  private Auth auth;

  public GetpackClient() {
    this("");
  }

  public GetpackClient(String baseUrl) {
    this.baseUrl = baseUrl != null ? baseUrl : "";
  }

  public Object put(String urlOrEndpoint) throws IOException {
    return put(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object put(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).put(requestBody).build();
    Response response = client.newCall(request).execute();
    return handleResponse(response, additionalParameters);
  }

  public Object put(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return put(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object put(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).put(requestBody).build();
    Response response = client.newCall(request).execute();
    return responseHandler.call(response);
  }

  public Object patch(String urlOrEndpoint) throws IOException {
    return patch(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object patch(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).patch(requestBody).build();
    Response response = client.newCall(request).execute();
    return handleResponse(response, additionalParameters);
  }

  public Object patch(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object patch(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).patch(requestBody).build();
    Response response = client.newCall(request).execute();
    return responseHandler.call(response);
  }

  public Object post(String urlOrEndpoint) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object post(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).post(requestBody).build();
    Response response = client.newCall(request).execute();
    return handleResponse(response, additionalParameters);
  }

  public Object post(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return post(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object post(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).post(requestBody).build();
    Response response = client.newCall(request).execute();
    return responseHandler.call(response);
  }

  public Object delete(String urlOrEndpoint) throws IOException {
    return delete(Collections.emptyMap(), urlOrEndpoint);
  }

  public Object delete(Map<?, ?> additionalParameters, String urlOrEndpoint) throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();

    Response response = client.newCall(request).execute();
    return handleResponse(response, additionalParameters);
  }

  public Object delete(String urlOrEndpoint, Closure<Void> responseHandler) throws IOException {
    return delete(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public Object delete(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<Void> responseHandler)
      throws IOException {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();
    Response response = client.newCall(request).execute();
    return responseHandler.call(response);
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
    return handleResponse(response, additionalParameters);
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
    Response response = client.newCall(request).execute();
    return responseHandler.call(response);
  }

  public void putDefaultHeader(String key, String value) {
    defaultHeaders.put(key, value);
  }

  public boolean removeDefaultHeader(String key) {
    return defaultHeaders.remove(key) != null;
  }

  private Object handleResponse(Response response, Map<?, ?> additionalParameters) throws IOException {
    ResponseBody body = response.body();
    if (body == null) {
      return null;
    }
    if (!response.isSuccessful()) {
      throw new ErrorResponseException(response);
    }
    Closure<?> contentResolver = getOrDefault(additionalParameters, "contentResolver", Closure.class, this.contentResolver);
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

  private RequestBody requestBody(Map<?, ?> additionalParameters) throws IOException {
    Object body = getOrDefault(additionalParameters, "body", Object.class, null);
    if (body == null) {
      return RequestBody.create(null, new byte[]{});
    }
    // this also handles MultipartBody
    if (body instanceof RequestBody) {
      return (MultipartBody) body;
    }
    if (contentType == null) {
      return RequestBody.create(String.valueOf(body).getBytes(StandardCharsets.UTF_8));
    }
    switch (contentType) {
      case JSON:
        String jsonBody;
        if (body instanceof CharSequence) {
          jsonBody = body.toString();
        } else {
          jsonBody = JsonOutput.toJson(body);
        }
        return RequestBody.create(jsonBody.getBytes(StandardCharsets.UTF_8), contentType.getMediaType());
      case XML:
        String xmlData;
        if (body instanceof CharSequence) {
          xmlData = body.toString();
        } else if (body instanceof Node) {
          xmlData = XmlUtil.serialize((Node) body);
        } else {
          throw new IllegalArgumentException("body must be a String or a groovy.util.Node to be serialized to XML");
        }
        return RequestBody.create(xmlData.getBytes(StandardCharsets.UTF_8), contentType.getMediaType());
      case TEXT:
      case HTML:
        return RequestBody.create(String.valueOf(body).getBytes(StandardCharsets.UTF_8), contentType.getMediaType());
      case BINARY:
        byte[] bytes;
        if (body instanceof byte[]) {
          bytes = (byte[]) body;
        } else if (body instanceof Byte[]) {
          Byte[] bytes1 = (Byte[]) body;
          bytes = new byte[bytes1.length];
          for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bytes1[i];
          }
        } else if (body instanceof InputStream) {
          bytes = IOGroovyMethods.getBytes((InputStream) body);
        } else {
          throw new IllegalArgumentException("body must be a byte array or an InputStream to be serialized to XML");
        }
        return RequestBody.create(bytes, contentType.getMediaType());
      default:
        throw new UnsupportedOperationException(contentType + " type is not handled");
    }
  }

  private Request.Builder request(String urlOrEndpoint, Map<?, ?> additionalParameters) {
    // url stuff
    String url = new UrlBuilder(baseUrl).append(urlOrEndpoint)
        .addParams(getOrDefault(additionalParameters, "params", Map.class, Collections.emptyMap()))
        .encoded();
    Request.Builder builder = new Request.Builder().url(url);
    // headers stuff
    for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }
    Map<?, ?> headers = getOrDefault(additionalParameters, "headers", Map.class, Collections.emptyMap());
    for (Map.Entry<?, ?> entry : headers.entrySet()) {
      builder.header(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
    }
    if (auth != null) {
      auth.apply(builder);
    }
    return builder;
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
