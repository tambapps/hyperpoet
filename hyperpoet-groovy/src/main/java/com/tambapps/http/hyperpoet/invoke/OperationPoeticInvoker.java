package com.tambapps.http.hyperpoet.invoke;

import com.tambapps.http.hyperpoet.HttpMethod;
import com.tambapps.http.hyperpoet.HttpPoet;
import groovy.lang.MissingMethodException;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Poetic invoker transforming operation name to endpoint
 */
@AllArgsConstructor
public class OperationPoeticInvoker implements PoeticInvoker<HttpPoet> {

  private final boolean useSForPosts;

  public OperationPoeticInvoker() {
    this(false);
  }

  @Override
  public Object invokeOrThrow(HttpPoet poet, String methodName, Object[] args,
      MissingMethodException e) throws IOException {
    // split words by upper case, including separators in words
    List<String> fields = Arrays.stream(methodName.split("(?=\\p{Upper})", -1))
        .map(s -> s.toLowerCase(Locale.ENGLISH))
        .collect(Collectors.toList());
    if (fields.size() < 2) {
      throw e;
    }
    HttpMethod method;
    switch (fields.get(0)) {
      case "get":
        method = HttpMethod.GET;
        break;
      case "modify":
      case "patch":
        method = HttpMethod.PATCH;
        break;
      case "put":
        method = HttpMethod.PUT;
        break;
      case "create":
      case "post":
        method = HttpMethod.POST;
        break;
      case "delete":
        method = HttpMethod.DELETE;
        break;
      default:
        throw e;
    }

    String endpoint = "/" + fields.stream().skip(1).collect(Collectors.joining("-"));
    if (useSForPosts && !fields.get(fields.size() - 1).endsWith("s") && method == HttpMethod.POST) {
      // endpoints for creating and use the plural
      endpoint += "s";
    }
    switch (args.length) {
      case 0:
        return poet.method(endpoint, method);
      case 1:
        if (args[0] instanceof Map) {
          return poet.method((Map) args[0], endpoint, method);
        } else {
          return poet.method(endpoint + "/" + args[0], method);
        }
      case 2:
        if (!(args[0] instanceof Map)) {
          throw new IllegalArgumentException("You should provide path variable and additional parameters");
        }
        return poet.method((Map) args[0], endpoint + "/" + args[1], method);
      default:
        throw new IllegalArgumentException("Too many arguments");
    }
  }
}
