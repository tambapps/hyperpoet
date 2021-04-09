package com.tambapps.http.getpack.auth;

import okhttp3.Request;

/**
 * Represents a way to authenticate against a server, through the request
 */
public interface Auth {

  String AUTHORIZATION_HEADER = "Authorization";

  /**
   * Apply the authentication to the request. Usually it adds a Authorization header
   * @param requestBuilder the request builder to apply the auth against
   */
  void apply(Request.Builder requestBuilder);

}
