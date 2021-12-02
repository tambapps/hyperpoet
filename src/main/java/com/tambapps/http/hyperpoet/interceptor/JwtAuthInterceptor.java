package com.tambapps.http.hyperpoet.interceptor;

import com.tambapps.http.hyperpoet.Headers;
import com.tambapps.http.hyperpoet.HttpPoet;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Interceptor that authenticate the request with an access_token.
 * Access token is automatically refreshed when expired, with the given tokenRefresher closure
 */
@RequiredArgsConstructor
public class JwtAuthInterceptor implements Interceptor {

  private final Closure<String> tokenRefresher;
  private final HttpPoet poet = new HttpPoet();
  private final AtomicReference<ExpirableToken> tokenReference = new AtomicReference<>();
  private final JsonSlurper slurper = new JsonSlurper();

  @NotNull
  @Override
  public Response intercept(@NotNull Chain chain) throws IOException {
    ExpirableToken token = tokenReference.get();
    if (isTokenExpired(token)) {
      synchronized (this) {
        tokenRefresher.setDelegate(this);
        String tokenString = tokenRefresher.call(poet);
        token = fromString(tokenString);
        tokenReference.set(token);
      }
    }

    Request request = chain.request()
        .newBuilder()
        .header(Headers.AUTHORIZATION, "Bearer " + token)
        .build();
    return chain.proceed(request);
  }

  private boolean isTokenExpired(ExpirableToken token) {
    return token == null || !LocalDateTime.now(Clock.systemUTC()).isBefore(token.getExpiresAt());
  }

  private ExpirableToken fromString(String token) throws IOException {
    return new ExpirableToken(token, parseExpClaim(token));
  }

  private LocalDateTime parseExpClaim(String token) throws IOException {
    String[] chunks = token.split("\\.");
    if (chunks.length < 2) {
      throw new IOException("Invalid JWT: doesn't have two chunks");
    }
    Base64.Decoder decoder = Base64.getDecoder();
    String payloadString;
    try {
      payloadString = new String(decoder.decode(chunks[1]));
    } catch (IllegalArgumentException e) {
      throw new IOException("Invalid JWT: not valid Base64");
    }
    Object payload = slurper.parseText(payloadString);
    Object expObject = InvokerHelper.getProperty(payload, "exp");
    if (!(expObject instanceof Number)) {
      throw new IOException("Invalid JWT: exp is not a number");
    }
    long exp = ((Number) expObject).longValue() ;
    return LocalDateTime.ofEpochSecond(exp, 0, ZoneOffset.UTC);
  }

  @Value
  private static class ExpirableToken {
    String token;
    LocalDateTime expiresAt;

    public String toString() {
      return token;
    }
  }
}
