package com.tambapps.http.hyperpoet.auth;

import lombok.AllArgsConstructor;
import okhttp3.Request;

/**
 * Represents a JWT Authentication by token
 */
@AllArgsConstructor
public class JwtAuth implements Auth {

  private final String jwtToken;

  @Override
  public void apply(Request.Builder builder) {
    builder.header(AUTHORIZATION_HEADER, String.format("Bearer %s", jwtToken));
  }
}
