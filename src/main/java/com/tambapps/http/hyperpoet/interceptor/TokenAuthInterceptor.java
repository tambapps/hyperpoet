package com.tambapps.http.hyperpoet.interceptor;

import com.tambapps.http.hyperpoet.Headers;
import com.tambapps.http.hyperpoet.HttpPoet;
import groovy.lang.Closure;
import lombok.Getter;
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

// TODO document this
/**
 * Interceptor that authenticate the request with a token. The token is refreshed when the client
 * get a 401 error.
 */
@RequiredArgsConstructor
public class TokenAuthInterceptor implements Interceptor {

  @Getter
  private final HttpPoet poet;
  private final String headerPrefix;
  private final Closure<String> tokenRefresher;
  private final AtomicReference<String> tokenReference = new AtomicReference<>();

  public TokenAuthInterceptor(Closure<String> tokenRefresher) {
    this("Bearer ", tokenRefresher);
  }
  public TokenAuthInterceptor(String headerPrefix, Closure<String> tokenRefresher) {
    this(new HttpPoet(), headerPrefix, tokenRefresher);
  }

  @NotNull
  @Override
  public Response intercept(@NotNull Chain chain) throws IOException {
    String token = tokenReference.updateAndGet(t -> t != null ? t : tokenRefresher.call(poet));

    Request request = chain.request()
        .newBuilder()
        .header(Headers.AUTHORIZATION, headerPrefix + token)
        .build();
    Response response = chain.proceed(request);
    if (response.code() == 401) {
      token = tokenReference.updateAndGet(t -> tokenRefresher.call(poet));
      response = chain.proceed(chain.request()
          .newBuilder()
          .header(Headers.AUTHORIZATION, headerPrefix + token)
          .build());
    }
    return response;
  }

  public String getToken() {
    return tokenReference.get();
  }

}
