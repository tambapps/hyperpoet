package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import lombok.Getter;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when an error response is received
 */
@Getter
public class ErrorResponseException extends RuntimeException {

  private final int code;
  private final ResponseBody body;
  private final Map<String, String> headers;

  protected ErrorResponseException(String method, String url, int code, ResponseBody body, Map<String, String> headers) {
    this(code, body, headers, String.format("endpoint %s %s response %d", method, url, code));
  }

  protected ErrorResponseException(int code, ResponseBody body, Map<String, String> headers, String message) {
    super(message);
    this.code = code;
    this.body = body;
    this.headers = headers;
  }

  public String getBodyAsText() throws IOException {
    return body.string();
  }

  public byte[] getBodyAsBytes() throws IOException {
    return body.bytes();
  }

  public InputStream getBodyAsInputStream() throws IOException {
    return body.byteStream();
  }

  /**
   * Get the body parsed with the provided parser
   * @param parser the parser closure
   * @return the parsed body
   */
  public Object getBody(Function parser) {
    return parser.call(body);
  }

  public static ErrorResponseException from(Response response) throws IOException {
    return new ErrorResponseException(response.request().method(), response.request().url().toString(),
        response.code(), response.body() != null ? CachedResponseBody.from(response.body()) : null, getHeaders(response));
  }

  protected static Map<String, String> getHeaders(Response response) {
    Map<String, String> headers = new HashMap<>();
    Headers okHeaders = response.headers();
    for (String name : okHeaders.names()) {
      headers.put(name, okHeaders.get(name));
    }
    return headers;
  }
}
