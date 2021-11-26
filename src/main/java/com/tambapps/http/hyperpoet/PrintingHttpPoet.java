package com.tambapps.http.hyperpoet;

import static com.tambapps.http.hyperpoet.util.Ansi.BLUE_SKY;
import static com.tambapps.http.hyperpoet.util.Ansi.RED;
import static com.tambapps.http.hyperpoet.util.Ansi.print;
import static com.tambapps.http.hyperpoet.util.Ansi.println;

import com.tambapps.http.hyperpoet.io.poeticprinter.PoeticPrinters;
import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import groovy.lang.Closure;
import groovy.transform.NamedParam;
import lombok.SneakyThrows;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;

// TODO document how to add printers
public class PrintingHttpPoet extends HttpPoet {

  private final Map<ContentType, Closure<?>> printers = PoeticPrinters.getMap();

  public PrintingHttpPoet(OkHttpClient okHttpClient, String baseUrl) {
    super(okHttpClient, baseUrl);
  }

  public PrintingHttpPoet(OkHttpClient client) {
    super(client);
  }

  public PrintingHttpPoet(
      @NamedParam(value = "okHttpClient", type = OkHttpClient.class)
      @NamedParam(value = "url", type = String.class)
      @NamedParam(value = "headers", type = Map.class)
      @NamedParam(value = "errorResponseHandler", type = Closure.class)
      @NamedParam(value = "onPreExecute", type = Closure.class)
      @NamedParam(value = "onPostExecute", type = Closure.class)
      @NamedParam(value = "acceptContentType", type = Closure.class)
      @NamedParam(value = "contentType", type = Closure.class)
      Map<?, ?> properties) {
    super(properties);
  }

  public PrintingHttpPoet(String baseUrl) {
    super(baseUrl);
  }

  @SneakyThrows
  @Override
  protected Object parseResponseBody(Response response, ResponseBody body,
      Map<?, ?> additionalParameters, ContentType responseContentType) {
    // cache response so we can print it and then reuse it for whatever the user will want to do
    CachedResponseBody cachedResponseBody = CachedResponseBody.fromResponseBody(body);
    printResponse(response, cachedResponseBody, responseContentType);
    return super.parseResponseBody(response, cachedResponseBody, additionalParameters,
        responseContentType);
  }

  private void printResponse(Response response, ResponseBody body,
      ContentType responseContentType) throws IOException {
    print("Response: ");
    String responseText = String.valueOf(response.code());
    String message = response.message();
    if (!message.isEmpty()) {
      responseText += " - " + message;
    }
    print(response.isSuccessful() ? BLUE_SKY : RED, responseText);
    println();
    byte[] bytes = body.bytes();
    printBytes(responseContentType, bytes);
  }

  @Override
  protected Object doRequest(Request request, Map<?, ?> additionalParameters)
      throws IOException {
    print(request.method().toUpperCase(Locale.ENGLISH) + " ");
    StringBuilder pathBuilder = new StringBuilder("/");
    HttpUrl url = request.url();
    pathBuilder.append(String.join("/", url.pathSegments()));
    int querySize = url.querySize();
    if (querySize > 0) {
      pathBuilder.append("?");
      for (int i = 0; i < querySize; i++) {
        pathBuilder.append(URLDecoder.decode(url.queryParameterName(i), "UTF-8"))
            .append("=")
            .append(URLDecoder.decode(String.valueOf(url.queryParameterValue(i)), "UTF-8"));
        if (i < querySize - 1) {
          pathBuilder.append("&");
        }
      }
    }
    print(BLUE_SKY, pathBuilder);
    println();
    if (request.body() != null) {
      if (request.body().isOneShot()) {
        print("Request is one shot. Cannot print it");
      } else {
        ContentType contentType = getOrDefault(additionalParameters, "contentType", ContentType.class,
            getContentType());
        byte[] bytes = extractRequestBody(request.body());
        printBytes(contentType, bytes);
      }
    }
    println();
    return super.doRequest(request, additionalParameters);
  }

  private void printBytes(ContentType contentType, byte[] bytes) {
    if (bytes.length == 0) {
      println("(No content)");
    } else {
      Closure<?> printer = printers.get(contentType);
      if (printer != null) {
        printer.call(bytes);
      }
    }
  }
}
