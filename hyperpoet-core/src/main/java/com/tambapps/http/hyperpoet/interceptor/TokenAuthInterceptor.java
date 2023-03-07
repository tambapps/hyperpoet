package com.tambapps.http.hyperpoet.interceptor;

import com.tambapps.http.hyperpoet.AbstractHttpPoet;
import com.tambapps.http.hyperpoet.Headers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

// TODO document this
/**
 * Interceptor that authenticate the request with a token. The token is refreshed when the client
 * get a 401 error.
 */
@RequiredArgsConstructor
public class TokenAuthInterceptor implements Interceptor {

  @Getter
  private final AbstractHttpPoet poet;
  private final String headerPrefix;
  private final Function<Object, ?> tokenRefresher;
  private final AtomicReference<String> tokenReference = new AtomicReference<>();

  public TokenAuthInterceptor(AbstractHttpPoet poet, Function<Object, ?> tokenRefresher) {
    this(poet, "Bearer ", tokenRefresher);
  }

  @NotNull
  @Override
  public Response intercept(@NotNull Chain chain) throws IOException {
    String token = tokenReference.updateAndGet(t -> t != null ? t :  tokenRefresher.apply(poet).toString());

    Request request = chain.request()
        .newBuilder()
        .header(Headers.AUTHORIZATION, headerPrefix + token)
        .build();
    Response response = chain.proceed(request);
    if (response.code() == 401) {
      token = tokenReference.updateAndGet(t -> tokenRefresher.apply(poet).toString());
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
