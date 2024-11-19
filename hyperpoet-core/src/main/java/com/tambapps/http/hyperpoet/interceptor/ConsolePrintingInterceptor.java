package com.tambapps.http.hyperpoet.interceptor;

import static com.tambapps.http.hyperpoet.util.Ansi.BLUE_SKY;
import static com.tambapps.http.hyperpoet.util.Ansi.RED;
import static com.tambapps.http.hyperpoet.util.Ansi.print;
import static com.tambapps.http.hyperpoet.util.Ansi.println;

import com.tambapps.http.hyperpoet.AbstractHttpPoet;
import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.io.json.JsonGenerator;
import com.tambapps.http.hyperpoet.io.poeticprinter.PoeticPrinters;
import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import com.tambapps.http.hyperpoet.util.ContentTypeMapFunction;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Interceptor allowing you to print colored request and response data. It is compatible and was only
 * tested in a Linux environment.
 * This interceptor can be useful for demos
 */
public class ConsolePrintingInterceptor implements Interceptor {

  /**
   * Whether to print full url or just the path
   */
  @Getter
  @Setter
  private boolean printFullUrl;
  // TODO document this
  @Getter
  @Setter
  private boolean printRequestHeaders;
  @Getter
  @Setter
  private boolean printResponseHeaders;
  @Getter
  private final ContentTypeMapFunction printers = PoeticPrinters.getMap(new JsonGenerator());

  private final AtomicBoolean shouldPrint = new AtomicBoolean(true);
  private final AtomicBoolean shouldPrintRequestBody = new AtomicBoolean(true);
  private final AtomicBoolean shouldPrintResponseBody = new AtomicBoolean(true);

  public ConsolePrintingInterceptor() {
    this(true);
  }

  public ConsolePrintingInterceptor(boolean printFullUrl) {
    this.printFullUrl = printFullUrl;
  }

  @NotNull
  @Override public Response intercept(@NotNull Chain chain) throws IOException {
    Request request = chain.request();
    if (!shouldPrint.get()) {
      return chain.proceed(request);
    }
    printRequest(request);
    return printAndCacheResponse(chain.proceed(request));
  }

  private void printRequest(Request request) throws IOException {
    print(request.method().toUpperCase(Locale.ENGLISH) + " ");
    StringBuilder endpointBuilder = new StringBuilder();
    HttpUrl url = request.url();
    if (printFullUrl) {
      endpointBuilder.append(url.scheme())
          .append("://")
          .append(url.host());
      if (url.port() != 80 && url.port() != 443) { // HTTP and HTTPS ports
        endpointBuilder.append(":").append(url.port());
      }
    }

    endpointBuilder.append('/').append(String.join("/", url.pathSegments()));
    int querySize = url.querySize();
    if (querySize > 0) {
      endpointBuilder.append("?");
      for (int i = 0; i < querySize; i++) {
        endpointBuilder.append(URLDecoder.decode(url.queryParameterName(i), "UTF-8"))
            .append("=")
            .append(URLDecoder.decode(String.valueOf(url.queryParameterValue(i)), "UTF-8"));
        if (i < querySize - 1) {
          endpointBuilder.append("&");
        }
      }
    }
    print(BLUE_SKY, endpointBuilder);
    println();
    if (printRequestHeaders) {
      printHeaders(request.headers());
    }
    if (!shouldPrintRequestBody.get()) {
      println();
      return;
    }
    RequestBody requestBody = request.body();
    if (requestBody != null) {
      if (requestBody.isOneShot()) {
        print("Request is one shot. Cannot print it");
      } else {
        String contentTypeHeader = request.headers().get(ContentType.HEADER);
        ContentType contentType = contentTypeHeader != null ? ContentType.valueOf(contentTypeHeader) : null;
        byte[] bytes = AbstractHttpPoet.extractRequestBody(requestBody);
        printBytes(bytes, contentType);
      }
    }
    println();
  }

  private Response printAndCacheResponse(Response response) throws IOException {
    print("Response: ");
    String responseText = String.valueOf(response.code());
    String message = response.message();
    if (!message.isEmpty()) {
      responseText += " - " + message;
    }
    print(response.isSuccessful() ? BLUE_SKY : RED, responseText);
    println();
    if (printResponseHeaders) {
      printHeaders(response.headers());
    }
    if (!shouldPrintResponseBody.get()) {
      return response;
    }

    Response cachedResponse = response.newBuilder().body(CachedResponseBody.from(response.body())).build();
    response.close();
    ResponseBody responseBody = cachedResponse.body();
    byte[] bytes = responseBody != null ? responseBody.bytes() : new byte[0];
    String contentTypeHeader = response.headers().get(ContentType.HEADER);
    // note that we don't know about the acceptContentType parameter here (it may be okay, so I may not ever fix this)
    ContentType contentType = contentTypeHeader != null ? ContentType.valueOf(contentTypeHeader) : null;
    printBytes(bytes, contentType);
    return cachedResponse;
  }

  private void printHeaders(Headers headers) {
    println("Headers");
    headers.forEach(p -> println(p.getFirst() + ": " + p.getSecond()));
    println();
  }

  private void printBytes(byte[] bytes, ContentType contentType) {
    if (bytes.length == 0) {
      println("(No content)");
    } else {
      Function<Object, ?> printer = printers.get(contentType);
      if (printer != null) {
        println(printer.apply(bytes));
      } else {
        println(new String(bytes));
      }
    }
  }

  public void setShouldPrint(boolean value) {
    shouldPrint.set(value);
  }

  public void setShouldPrintRequestBody(boolean value) {
    shouldPrintRequestBody.set(value);
  }

  public void setShouldPrintResponseBody(boolean value) {
    shouldPrintResponseBody.set(value);
  }
}
