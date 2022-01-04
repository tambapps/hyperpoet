package com.tambapps.http.hyperpoet;

import groovy.lang.Closure;
import lombok.Getter;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Map;

/**
 * Error response exception containing data for JSON RFC 7807 Problem details
 */
public class ProblemResponseException extends ErrorResponseException {

  @Getter
  private final String type;
  @Getter
  private final String detail;

  protected ProblemResponseException(int code, byte[] body,
      Map<String, String> headers, String message, String type, String detail) {
    super(code, body, headers, message);
    this.type = type;
    this.detail = detail;
  }

  public static ProblemResponseException from(Response response, Closure<?> parser) throws IOException {
    ResponseBody responseBody = response.body();
    byte[] bytes = null;
    Request request = response.request();
    String message = String.format("endpoint %s %s responded %d", request.method(), request.url().encodedPath(), response.code());
    String problemType = null;
    String problemDetail = null;
    if (responseBody != null) {
      bytes = responseBody.bytes();
      try {
        Map<?, ?> json = (Map<?, ?>) parser.call(response);
        problemType = String.valueOf(json.get("type"));
        problemDetail = String.valueOf(json.get("detail"));
        message = String.format("endpoint %s %s responded %d with problem type %s: %s", request.method(), request.url().encodedPath(), response.code(), problemType, problemDetail);
      } catch (Exception ignored) {
      }
    }
    return new ProblemResponseException(response.code(), bytes, ErrorResponseException.getHeaders(response), message, problemType, problemDetail);
  }

}
