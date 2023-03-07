package com.tambapps.http.hyperpoet.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tambapps.http.hyperpoet.AbstractHttpPoet;
import com.tambapps.http.hyperpoet.Headers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Interceptor that authenticate the request with an access_token.
 * Access token is automatically refreshed when expired, with the given tokenRefresher closure
 */
@RequiredArgsConstructor
public class JwtAuthInterceptor implements Interceptor {

  @Getter
  private final AbstractHttpPoet poet;
  private final Function<Object, ?> tokenRefresher;
  private final AtomicReference<ExpirableToken> tokenReference = new AtomicReference<>();
  private final ObjectMapper mapper = new ObjectMapper();

  public JwtAuthInterceptor(Function<Object, ?> tokenRefresher) {
    this(AbstractHttpPoet.newPoet(), tokenRefresher);
  }

  @NotNull
  @Override
  public Response intercept(@NotNull Chain chain) throws IOException {
    ExpirableToken token = tokenReference.get();
    if (isTokenExpired(token)) {
      synchronized (this) {
        token = refreshToken();
      }
    }

    Request request = chain.request()
        .newBuilder()
        .header(Headers.AUTHORIZATION, "Bearer " + token)
        .build();
    return chain.proceed(request);
  }

  public ExpirableToken refreshToken() throws IOException {
    String tokenString = (String) tokenRefresher.apply(poet);
    ExpirableToken token = fromString(tokenString);
    tokenReference.set(token);
    return token;
  }

  public ExpirableToken getToken() {
    return tokenReference.get();
  }

  private boolean isTokenExpired(ExpirableToken token) {
    return token == null || !LocalDateTime.now(Clock.systemUTC()).isBefore(token.getExpiresAt());
  }

  private ExpirableToken fromString(String token) throws IOException {
    if (token == null) {
      throw new IOException("Token is null");
    }
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
    Map<?, ?> payload = mapper.readValue(payloadString, Map.class);
    Object expObject = payload.get("exp");
    if (!(expObject instanceof Number)) {
      throw new IOException("Invalid JWT: exp is not a number");
    }
    long exp = ((Number) expObject).longValue() ;
    return LocalDateTime.ofEpochSecond(exp, 0, ZoneOffset.UTC);
  }

  @Value
  public static class ExpirableToken {
    String token;
    LocalDateTime expiresAt;

    public String toString() {
      return token;
    }
  }
}
