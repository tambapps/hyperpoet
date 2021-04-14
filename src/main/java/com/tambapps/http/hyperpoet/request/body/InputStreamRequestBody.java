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

  public InputStreamRequestBody(Closure<?> inputStreamSupplier) {
    this(inputStreamSupplier, (MediaType) null);
  }

  public InputStreamRequestBody(Closure<?> inputStreamSupplier, ContentType contentType) {
    this(inputStreamSupplier, MediaType.get(contentType.toString()));
  }

  public InputStreamRequestBody(Closure<?> inputStreamSupplier, MediaType mediaType) {
    this.inputStreamSupplier = inputStreamSupplier;
    this.mediaType = mediaType;
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
}
