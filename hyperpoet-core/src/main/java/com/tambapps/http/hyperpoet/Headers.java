package com.tambapps.http.hyperpoet;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public final class Headers {

  public static String AUTHORIZATION = "Authorization";

  private Headers() {}

  public static List<String> basicAuth(String user, String password) {
   return basicAuth(user + ":" + password);
  }

  public static List<String> basicAuth(String credentials) {
    return authorization(String.format("Basic %s", Base64.getEncoder()
        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8))));
  }

  public static List<String> bearer(String token) {
    return authorization(String.format("Bearer %s", token));
  }

  public static List<String> authorization(String value) {
    return Arrays.asList(AUTHORIZATION, value);
  }


}
