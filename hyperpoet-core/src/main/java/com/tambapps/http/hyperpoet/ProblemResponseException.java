package com.tambapps.http.hyperpoet;

import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import lombok.Getter;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Error response exception containing data for JSON RFC 7807 Problem details
 */
public class ProblemResponseException extends ErrorResponseException {

  @Getter
  private final Map<String, Object> members;

  protected ProblemResponseException(int code, ResponseBody body,
      Map<String, String> headers, String message, Map<String, Object> members) {
    super(code, body, headers, message);
    this.members = members;
  }

  public String getType() {
    return (String) members.get("type");
  }

  public String getTitle() {
    return (String) members.get("title");
  }

  public String getDetail() {
    return (String) members.get("detail");
  }

  public Integer getStatus() {
    Number number = (Number) members.get("status");
    return number != null ? number.intValue() : null;
  }

  public String getInstance() {
    return (String) members.get("instance");
  }

  public Object getMember(String propertyName) {
    return members.get(propertyName);
  }

  public Object getAt(String propertyName) {
    return getMember(propertyName);
  }

  public static ProblemResponseException from(Response response, Function parser) throws IOException {
    CachedResponseBody responseBody = CachedResponseBody.from(response.body());
    Map<String, Object> members = new HashMap<>();
    Request request = response.request();
    String errorMessage = String.format("endpoint %s %s responded %d", request.method(), request.url().encodedPath(), response.code());
    if (!responseBody.isEmpty()) {
      try {
        Map<?, ?> json = (Map<?, ?>) parser.call(responseBody);
        json.forEach((key, value) -> members.put(String.valueOf(key), value));
        errorMessage = String.format("endpoint %s %s responded %d with problem type %s: %s", request.method(), request.url().encodedPath(), response.code(), members.get("type"), members.get("detail"));
      } catch (Exception ignored) {
      }
    }
    return new ProblemResponseException(response.code(), responseBody, ErrorResponseException.getHeaders(response), errorMessage, members);
  }

}
