package com.tambapps.http.getpack.auth;

import okhttp3.Request;

public interface Auth {

  String AUTHORIZATION_HEADER = "Authorization";

  void apply(Request.Builder requestBuilder);

}
