package com.tambapps.http.hyperpoet.io.composer;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.FormPart;
import com.tambapps.http.hyperpoet.io.IoUtils;
import com.tambapps.http.hyperpoet.io.json.JsonGenerator;
import com.tambapps.http.hyperpoet.url.QueryParamComposer;
import com.tambapps.http.hyperpoet.util.ContentTypeMapFunction;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class holding several common composers.
 * An composer should return one of the following types
 * - a byte array (primitive byte, not Byte)
 * - an InputStream
 * - a String
 * - A okhttp3.RequestBody
 *
 */
public final class Composers {

  private Composers() {}

  public static ContentTypeMapFunction getMap(JsonGenerator jsonGenerator, QueryParamComposer queryParamComposer) {
    ContentTypeMapFunction map = new ContentTypeMapFunction();
    map.put(ContentType.JSON, jsonGenerator::composeToJson);
    map.put(ContentType.TEXT, Composers::composeStringBody);
    map.put(ContentType.HTML, Composers::composeStringBody);
    map.put(ContentType.BINARY, Composers::composeBytesBody);
    map.put(ContentType.URL_ENCODED, queryParamComposer::composeToString);
    map.put(ContentType.MULTIPART_FORM, new MultipartFormComposerClosure(map));
    // default composer (when no content type was found)
    map.setDefaultValue(Composers::composeStringBody);
    return map;
  }

  public static String composeStringBody(Object body) {
    return String.valueOf(body);
  }

  @SneakyThrows
  public static byte[] composeBytesBody(Object body) {
    return IoUtils.rawToBytes(body);
  }

  private static class MultipartFormComposerClosure implements Function<Object, RequestBody> {
    // keeping poet instance to have an up to date (and mostly non-null) composers
    private final ContentTypeMapFunction composers;

    MultipartFormComposerClosure(ContentTypeMapFunction composers) {
      this.composers = composers;
    }

    @SneakyThrows
    @Override
    public RequestBody apply(Object body) {
      Map<?, ?> map;
      if (body instanceof File) {
        map = Collections.singletonMap(((File) body).getName(), body);
      } else if (body instanceof Path) {
        map = Collections.singletonMap(((Path) body).toFile().getName(), body);
      } else if (body instanceof Map) {
        map = (Map<?, ?>) body;
      } else {
        throw new IllegalArgumentException("Body must be a file, a path or a map");
      }
      MultipartBody.Builder builder = new MultipartBody.Builder()
          .setType(MultipartBody.FORM);

      for (Map.Entry<?, ?> entry : map.entrySet()) {
        String key = String.valueOf(entry.getKey());
        Object value = entry.getValue();
        if (value instanceof File) {
          addFormDataPart(builder, key, (File) value);
        } else if (value instanceof Path) {
          addFormDataPart(builder, key, ((Path) body).toFile());
        } else if (value instanceof InputStream) {
          addFormDataPart(builder, key, (InputStream) value);
        } else if (value instanceof FormPart) {
          addFormDataPart(builder, key, (FormPart) value);
        } else {
          builder.addFormDataPart(key, String.valueOf(value));
        }
      }
      return builder.build();
    }

    private void addFormDataPart(MultipartBody.Builder builder, String key, File file) throws IOException {
      builder.addFormDataPart(key, file.getName(), RequestBody.create(IoUtils.getBytes(file), ContentType.BINARY.toMediaType()));
    }

    private void addFormDataPart(MultipartBody.Builder builder, String key, InputStream inputStream) throws IOException {
      builder.addFormDataPart(key, key, RequestBody.create(IoUtils.getBytes(inputStream), ContentType.BINARY.toMediaType()));
    }

    private void addFormDataPart(MultipartBody.Builder builder, String key, FormPart formPart) throws IOException {
      final Object value = formPart.getValue();
      MediaType mediaType = formPart.getContentType().toMediaType();
      String filename = formPart.getFilename() != null ? formPart.getFilename() : key;
      RequestBody requestBody;
      // smart conversions
      if (value instanceof File) {
        requestBody = RequestBody.create(IoUtils.getBytes((File) value), mediaType);
      } else if (value instanceof Path) {
        requestBody = RequestBody.create(IoUtils.getBytes(((Path) value).toFile()), mediaType);;
      } else if (value instanceof Reader) {
        requestBody = RequestBody.create(IoUtils.getText((Reader) value), mediaType);
      } else {
        Function composer = composers.get(formPart.getContentType());
        if (composer == null) {
          throw new IllegalArgumentException(String.format("Couldn't find a composer for FormPart '%s'", filename));
        }
        requestBody = (RequestBody) composer.apply(value);
      }
      builder.addFormDataPart(key, filename, requestBody);
    }
  }
}
