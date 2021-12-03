package com.tambapps.http.hyperpoet.invoke;

import com.tambapps.http.hyperpoet.HttpMethod;
import com.tambapps.http.hyperpoet.HttpPoet;
import groovy.lang.MissingMethodException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class OpenApiSpecPoeticInvoker implements PoeticInvoker {

  private final Map<String, EndpointOperation> endpointOperationMap;

  public static OpenApiSpecPoeticInvoker fromSpec(String spec) throws IOException{
    ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(true);
    SwaggerParseResult result = new OpenAPIV3Parser().readContents(spec, null, parseOptions);
    if (result.getMessages() != null && !result.getMessages().isEmpty()) {
      throw new IOException("Error while parsing spec: " + String.join(", ", result.getMessages()));
    }

    Map<String, EndpointOperation> endpointOperationMap = new HashMap<>();
    for (Map.Entry<String, PathItem> entry : result.getOpenAPI().getPaths().entrySet()) {
      String path = entry.getKey();
      PathItem item = entry.getValue();
      addOperation(endpointOperationMap, path, item.getGet(), HttpMethod.GET);
      addOperation(endpointOperationMap, path, item.getDelete(), HttpMethod.DELETE);
      addOperation(endpointOperationMap, path, item.getPost(), HttpMethod.POST);
      addOperation(endpointOperationMap, path, item.getPut(), HttpMethod.PUT);
      addOperation(endpointOperationMap, path, item.getPatch(), HttpMethod.PATCH);
    }

    return new OpenApiSpecPoeticInvoker(Collections.unmodifiableMap(endpointOperationMap));
  }

  private static void addOperation(Map<String, EndpointOperation> endpointOperationMap, String path, Operation operation, HttpMethod method) {
    if (operation == null) return;
    endpointOperationMap.put(operation.getOperationId(), new EndpointOperation(path, method, operation));
  }

  @Override
  public Object invokeOrThrow(HttpPoet poet, String methodName, Object[] args,
      MissingMethodException e) throws IOException {
    EndpointOperation op = endpointOperationMap.get(methodName);
    if (op == null) {
      throw e;
    }

    Map<String, Object> additionalParams = new HashMap<>();
    // TODO do query params, path parameters, etc...

    return poet.method(additionalParams, op.getPath(), op.getMethod());
  }

  @Value
  private static class EndpointOperation {
    String path;
    HttpMethod method;
    Operation operation;
  }

}
