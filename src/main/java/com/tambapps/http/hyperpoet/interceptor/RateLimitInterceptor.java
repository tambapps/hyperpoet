package com.tambapps.http.hyperpoet.interceptor;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

/**
 * Interceptor that execute requests and waits for the rate limit to expire when there is one
 */
public class RateLimitInterceptor implements Interceptor {

  private static final String RETRY_AFTER_HEADER = "Retry-After";

  @NotNull
  @Override
  public Response intercept(@NotNull Chain chain) throws IOException {
    Response response = chain.proceed(chain.request());
    if (response.code() != 429) {
      return response;
    }
    Long retryAfterSeconds = parseRetryAfterSeconds(response.header(RETRY_AFTER_HEADER));
    if (retryAfterSeconds == null) {
      // can't know when to retry? then can't apply this filter
      return response;
    }
    // waiting for rate limit to expire, + one second just to be sure
    try {
      Thread.sleep(retryAfterSeconds * 1000L + 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return chain.proceed(chain.request());
  }

  /**
   * Parse Retry-After header. It can be a number representing the number of seconds to wait, or
   * a RFC_1123_DATE_TIME
   *
   * @param header the header value
   * @return the number of second to wait before retrying
   */
  private Long parseRetryAfterSeconds(String header) {
    try {
      return Long.parseLong(header);
    } catch (NumberFormatException e) { }
    try {
      TemporalAccessor retryAt = DateTimeFormatter.RFC_1123_DATE_TIME.parse(header);
      return Duration.between(Instant.now(), Instant.from(retryAt)).getSeconds();
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
