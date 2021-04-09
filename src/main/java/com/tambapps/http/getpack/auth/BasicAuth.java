package com.tambapps.http.getpack.auth;

import okhttp3.Request;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Represents a Basic Authentication
 */
public class BasicAuth implements Auth {

  private final String authHeader;

  /**
   * Constructs a basic authentication with the given username and password. They are not stored in
   * memory. Only the (Base64 encoded) header is kept
   * @param username the username
   * @param password the password
   */
  public BasicAuth(String username, String password) {
    authHeader = String.format("Basic %s", Base64.getEncoder()
        .encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8)));
  }

  @Override
  public void apply(Request.Builder builder) {
    builder.header(AUTHORIZATION_HEADER, authHeader);
  }
}
