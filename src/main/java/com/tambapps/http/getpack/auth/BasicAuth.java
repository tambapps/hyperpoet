package com.tambapps.http.getpack.auth;

import okhttp3.Request;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuth implements Auth {

  private final String authHeader;

  public BasicAuth(String username, String password) {
    authHeader = String.format("Basic %s", Base64.getEncoder()
        .encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8)));
  }

  @Override
  public void apply(Request.Builder builder) {
    builder.header(AUTHORIZATION_HEADER, authHeader);
  }
}
