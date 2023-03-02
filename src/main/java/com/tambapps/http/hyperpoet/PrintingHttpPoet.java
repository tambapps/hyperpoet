package com.tambapps.http.hyperpoet;

import static com.tambapps.http.hyperpoet.util.Ansi.BLUE_SKY;
import static com.tambapps.http.hyperpoet.util.Ansi.RED;
import static com.tambapps.http.hyperpoet.util.Ansi.print;
import static com.tambapps.http.hyperpoet.util.Ansi.println;
import static com.tambapps.http.hyperpoet.util.ParametersUtils.getOrDefault;
import static com.tambapps.http.hyperpoet.util.ParametersUtils.getOrDefaultSupply;

import com.tambapps.http.hyperpoet.io.poeticprinter.PoeticPrinters;
import com.tambapps.http.hyperpoet.util.CachedResponseBody;
import com.tambapps.http.hyperpoet.util.ContentTypeMapFunction;
import groovy.lang.Closure;
import groovy.transform.NamedParam;
import lombok.Getter;
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

/**
 * Http poet printing request and response data.
 * Deprecated: use HttpPoet with a ConsolePrintingInterceptor instead
 */
@Deprecated
@Getter
public class PrintingHttpPoet extends HttpPoet {

  private final ContentTypeMapFunction printers = PoeticPrinters.getMap();

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

  @Override
  @SneakyThrows
  protected Object handleResponse(Response response, Map<?, ?> additionalParameters) {
    if (getOrDefault(additionalParameters, "print", Boolean.class, true)) {
      final Response cachedResponse = response.newBuilder().body(CachedResponseBody.from(response.body())).build();
      response = cachedResponse;
      ContentType responseContentType =
          getOrDefaultSupply(additionalParameters, "acceptContentType", ContentType.class, () -> getResponseContentType(cachedResponse));
      printResponse(cachedResponse, cachedResponse.body(), responseContentType, additionalParameters);
    }
    return super.handleResponse(response, additionalParameters);
  }

  @Override
  protected Object defaultHandleErrorResponse(Response response, Map<?, ?> additionalParameters) {
    return response.body() != null ? parseResponse(response, additionalParameters) : null;
  }

  private void printResponse(Response response, ResponseBody body,
      ContentType responseContentType, Map<?, ?> additionalParameters) throws IOException {
    print("Response: ");
    String responseText = String.valueOf(response.code());
    String message = response.message();
    if (!message.isEmpty()) {
      responseText += " - " + message;
    }
    print(response.isSuccessful() ? BLUE_SKY : RED, responseText);
    println();
    byte[] bytes = body.bytes();
    printBytes(responseContentType, bytes, getOrDefault(additionalParameters, "printResponseBody", Boolean.class, true));
  }

  @Override
  protected Object doRequest(Request request, Map<?, ?> additionalParameters)
      throws IOException {
    boolean print = getOrDefault(additionalParameters, "print", Boolean.class, true);
    if (!print) {
      return super.doRequest(request, additionalParameters);
    }
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
        printBytes(contentType, bytes, getOrDefault(additionalParameters, "printRequestBody", Boolean.class, true));
      }
    }
    println();
    return super.doRequest(request, additionalParameters);
  }

  private void printBytes(ContentType contentType, byte[] bytes, boolean print) {
    if (bytes.length == 0) {
      println("(No content)");
    } else if (print) {
      Function printer = printers.get(contentType);
      if (printer != null) {
        printer.call(bytes);
      } else {
        println(new String(bytes));
      }
    }
  }

  public void setDefaultPrinter(Function closure) {
    printers.setDefaultValue(closure);
  }
}
