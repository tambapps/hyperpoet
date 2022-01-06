package com.tambapps.http.hyperpoet.util;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;


@AllArgsConstructor
public class CachedResponseBody extends ResponseBody {

  public static CachedResponseBody fromResponseBody(ResponseBody responseBody) throws IOException {
    if (responseBody instanceof CachedResponseBody) {
      return (CachedResponseBody) responseBody;
    }
    long contentLength = responseBody.contentLength();
    MediaType contentType = responseBody.contentType();
    return new CachedResponseBody(contentLength, contentType, responseBody.bytes());
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
}
