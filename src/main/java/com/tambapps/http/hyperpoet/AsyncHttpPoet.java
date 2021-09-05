package com.tambapps.http.hyperpoet;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class AsyncHttpPoet extends HttpPoet {

  public static final int REQUEST_NOT_EXECUTED_CODE = 0;

  private ExecutorService executor;
  private boolean includeResponse;

  public AsyncHttpPoet() {
    this("");
  }

  public AsyncHttpPoet(OkHttpClient client) {
    this(client, "");
  }

  public AsyncHttpPoet(Map<?, ?> properties) {
    super(properties);
    if (properties.get("executor") instanceof ExecutorService) {
      executor = (ExecutorService) properties.get("executor");
    }
    if (properties.get("includeResponse") instanceof Boolean) {
      includeResponse = (Boolean) properties.get("includeResponse");
    }
  }

  public AsyncHttpPoet(String baseUrl) {
    this(new OkHttpClient(), baseUrl);
  }

  public AsyncHttpPoet(OkHttpClient okHttpClient, String baseUrl) {
    super(okHttpClient, baseUrl);
  }

  public void getAsync(String urlOrEndpoint, Closure<?> responseHandler) {
    getAsync(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public void getAsync(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler) {
    Request request = request(urlOrEndpoint, additionalParameters).get().build();
    doRequestAsync(request, responseHandler, additionalParameters);
  }

  public void deleteAsync(String urlOrEndpoint, Closure<?> responseHandler) {
    deleteAsync(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public void deleteAsync(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler) {
    Request request = request(urlOrEndpoint, additionalParameters).delete().build();
    doRequestAsync(request, responseHandler, additionalParameters);
  }

  public void postAsync(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    postAsync(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public void postAsync(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).post(requestBody).build();
    doRequestAsync(request, responseHandler, additionalParameters);
  }

  public void patchAsync(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    patchAsync(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public void patchAsync(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).patch(requestBody).build();
    doRequestAsync(request, responseHandler, additionalParameters);
  }

  public void putAsync(String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    putAsync(Collections.emptyMap(), urlOrEndpoint, responseHandler);
  }

  public void putAsync(Map<?, ?> additionalParameters, String urlOrEndpoint, Closure<?> responseHandler) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).put(requestBody).build();
    doRequestAsync(request, responseHandler, additionalParameters);
  }

  public void methodAsync(String urlOrEndpoint, String method, Closure<?> responseHandler) throws IOException {
    methodAsync(Collections.emptyMap(), urlOrEndpoint, method, responseHandler);
  }

  public void methodAsync(Map<?, ?> additionalParameters, String urlOrEndpoint, String method, Closure<?> responseHandler) throws IOException {
    RequestBody requestBody = requestBody(additionalParameters);
    Request request = request(urlOrEndpoint, additionalParameters).method(method, requestBody).build();
    doRequestAsync(request, responseHandler, additionalParameters);
  }

  private void doRequestAsync(Request request, Closure<?> responseHandler,
      Map<?, ?> additionalParameters) {
    if (executor == null) {
      executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }
    boolean includeResponse = additionalParameters.get("includeResponse") instanceof Boolean ?
        (Boolean) additionalParameters.get("includeResponse") : this.includeResponse;

    executor.submit(() -> {
      try {
        if (includeResponse) {
          doRequest(request, responseHandler);
        } else {
          responseHandler.call(doRequest(request, additionalParameters));
        }
      } catch (IOException e) {
        if (includeResponse) {
          // exception were thrown while executing request, let's respond with a fake response
          Response response = new Response.Builder().code(REQUEST_NOT_EXECUTED_CODE).request(request)
              .protocol(Protocol.HTTP_1_0)
              .message(e.getMessage())
              .build();
          responseHandler.call(response);
        } else {
          responseHandler.call(new Object[] { null });
        }
      }
    });
  }

  public void shutdown() {
    executor.shutdown();
  }

  public void shutdownNow() {
    executor.shutdownNow();
  }
}
