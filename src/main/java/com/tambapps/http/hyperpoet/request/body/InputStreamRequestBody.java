package com.tambapps.http.hyperpoet.request.body;

import com.tambapps.http.hyperpoet.ContentType;
import groovy.lang.Closure;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamRequestBody extends RequestBody {

  private final Closure<?> inputStreamSupplier;
  private final MediaType mediaType;
  private final boolean oneShot;

  public InputStreamRequestBody(Closure<?> inputStreamSupplier) {
    this(inputStreamSupplier, (MediaType) null, false);
  }

  public InputStreamRequestBody(Closure<?> inputStreamSupplier, ContentType contentType, boolean oneShot) {
    this(inputStreamSupplier, MediaType.get(contentType.toString()), oneShot);
  }

  public InputStreamRequestBody(Closure<?> inputStreamSupplier, MediaType mediaType, boolean oneShot) {
    this.inputStreamSupplier = inputStreamSupplier;
    this.mediaType = mediaType;
    this.oneShot = oneShot;
  }

  @Nullable
  @Override
  public MediaType contentType() {
    return mediaType;
  }

  @Override
  public void writeTo(@NotNull BufferedSink sink) throws IOException {
    try (InputStream inputStream = (InputStream) inputStreamSupplier.call()) {
      sink.writeAll(Okio.source(inputStream));
    }
  }

  @Override
  public boolean isOneShot() {
    return oneShot;
  }
}
