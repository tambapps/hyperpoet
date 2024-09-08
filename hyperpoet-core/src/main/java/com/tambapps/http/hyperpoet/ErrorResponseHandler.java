package com.tambapps.http.hyperpoet;

import okhttp3.Response;

public interface ErrorResponseHandler {

  default void init(AbstractHttpPoet poet) {}

  Object handle(Response response);

}
