package com.tambapps.http.hyperpoet;

import groovy.lang.Tuple2;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Headers {

  public static String AUTHORIZATION_HEADER = "Authorization";

  private Headers() {}

  public static Tuple2<String, String> basicAuth(String user, String password) {
   return basicAuth(user + ":" + password);
  }

  public static Tuple2<String, String> basicAuth(String credentials) {
    return authorization(String.format("Basic %s", Base64.getEncoder()
        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8))));
  }

  public static Tuple2<String, String> jwt(String jwtToken) {
    return authorization(String.format("Bearer %s", jwtToken));
  }

  public static Tuple2<String, String> authorization(String value) {
    return new Tuple2<>(AUTHORIZATION_HEADER, value);
  }


}
