package com.tambapps.http.hyperpoet.util;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@AllArgsConstructor
public class CachedResponseBody extends ResponseBody {

  public static Response newResponseWitchCachedBody(Response response) throws IOException {
    if (response.body() instanceof CachedResponseBody) {
      return response;
    } else {
      return new Response(response.request(), response.protocol(), response.message(), response.code(),
          response.handshake(), response.headers(), from(response.body()), response.networkResponse(),
          null, null, response.sentRequestAtMillis(), response.receivedResponseAtMillis(),
          null);
    }
  }

  public static CachedResponseBody from(ResponseBody responseBody) throws IOException {
    if (responseBody == null) {
      return new CachedResponseBody(0L, null, new byte[0]);
    } else if (responseBody instanceof CachedResponseBody) {
      return (CachedResponseBody) responseBody;
    } else {
      long contentLength = responseBody.contentLength();
      MediaType contentType = responseBody.contentType();
      return new CachedResponseBody(contentLength, contentType, responseBody.bytes());
    }
  }

  private long contentLength;
  private MediaType contentType;
  private byte[] bytes;

  @Override
  public long contentLength() {
    return contentLength;
  }

  @Nullable
  @Override
  public MediaType contentType() {
    return contentType;
  }

  @NotNull
  @Override
  public BufferedSource source() {
    return Okio.buffer(Okio.source(new ByteArrayInputStream(bytes)));
  }

  public boolean isEmpty() {
    return contentLength() == 0L;
  }
}
