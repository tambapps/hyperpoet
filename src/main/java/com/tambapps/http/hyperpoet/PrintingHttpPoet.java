package com.tambapps.http.hyperpoet;

import static com.tambapps.http.hyperpoet.util.Ansi.BLUE_SKY;
import static com.tambapps.http.hyperpoet.util.Ansi.RED;
import static com.tambapps.http.hyperpoet.util.Ansi.print;
import static com.tambapps.http.hyperpoet.util.Ansi.println;

import com.tambapps.http.hyperpoet.io.parser.PrettyPrintJsonParserClosure;
import com.tambapps.http.hyperpoet.io.json.PrettyJsonGenerator;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.transform.NamedParam;
import lombok.SneakyThrows;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PrintingHttpPoet extends HttpPoet {

  private final JsonSlurper jsonSlurper = new JsonSlurper();
  private final PrettyJsonGenerator prettyJsonGenerator = new PrettyJsonGenerator();

  public PrintingHttpPoet(OkHttpClient okHttpClient, String baseUrl) {
    super(okHttpClient, baseUrl);
    init();
  }

  public PrintingHttpPoet() {
    init();
  }

  public PrintingHttpPoet(OkHttpClient client) {
    super(client);
    init();
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
    init();
  }

  public PrintingHttpPoet(String baseUrl) {
    super(baseUrl);
    init();
  }

  private void init() {
    onPostExecute = new MethodClosure(this, "doOnPostExecute");
    getParsers().put(ContentType.JSON, new PrettyPrintJsonParserClosure(jsonSlurper, prettyJsonGenerator));
  }

  // used by method closure
  private Object parseJsonResponseBody(ResponseBody body) throws IOException {
    String text = body.string();
    if (text.isEmpty()) {
      return "(No content)";
    }
    Object object = jsonSlurper.parseText(text);
    print(prettyJsonGenerator.toJson(object));
    println();
    return object;
  }

  private Object parseAndPrintJson(ResponseBody body) throws IOException {
    String text = body.string();
    if (text.isEmpty()) {
      return "(No content)";
    }
    Object object = jsonSlurper.parseText(text);
    print(prettyJsonGenerator.toJson(object));
    println();
    return object;
  }

  // used by method closure
  private void doOnPostExecute(Response response) {
    print("Response: ");
    String responseText = String.valueOf(response.code());
    String message = response.message();
    if (!message.isEmpty()) {
      responseText += " - " + message;
    }
    print(response.isSuccessful() ? BLUE_SKY : RED, responseText);
    println();
  }

  @SneakyThrows
  @Override
  protected Object handleErrorResponse(Response response) {
    if (response.body() == null) {
      return null;
    }
    return parseAndPrintJson(response.body());
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
    if (request.body() != null && !request.body().isOneShot()) {
      Object contentType = additionalParameters.get("contentType");
      if (contentType == ContentType.JSON || (contentType == null && this.getContentType() == ContentType.JSON)) {
        String s = new String(extractRequestBody(request.body()));
        if (!s.trim().isEmpty()) {
          print(prettyJsonGenerator.toJson(jsonSlurper.parseText(new String(extractRequestBody(request.body())))));
        }
      } else {
        print(new String(extractRequestBody(request.body())));
      }
      println();
    }
    println();
    return super.doRequest(request, additionalParameters);
  }

}
