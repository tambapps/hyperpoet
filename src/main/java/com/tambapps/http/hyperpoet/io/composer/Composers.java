package com.tambapps.http.hyperpoet.io.composer;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.FormPart;
import com.tambapps.http.hyperpoet.HttpPoet;
import com.tambapps.http.hyperpoet.url.QueryParamComposer;
import groovy.json.JsonGenerator;
import groovy.json.JsonOutput;
import groovy.lang.Closure;
import groovy.util.Node;
import groovy.xml.XmlUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

  public static Map<ContentType, Closure<?>> getMap(HttpPoet httpPoet,
      JsonGenerator jsonGenerator, QueryParamComposer queryParamComposer) {
    Map<ContentType, Closure<?>> map = new HashMap<>();
    map.put(ContentType.JSON, new MethodClosure(jsonGenerator, "toJson"));
    map.put(ContentType.XML, new MethodClosure(Composers.class, "composeXmlBody"));
    map.put(ContentType.TEXT, new MethodClosure(Composers.class, "composeStringBody"));
    map.put(ContentType.HTML, new MethodClosure(Composers.class, "composeStringBody"));
    map.put(ContentType.BINARY, new MethodClosure(Composers.class, "composeBytesBody"));
    map.put(ContentType.URL_ENCODED, new MethodClosure(queryParamComposer, "composeToString"));
    map.put(ContentType.MULTIPART_FORM, new MultipartFormComposerClosure(httpPoet));
    // default composer (when no content type was found)
    map.put(null, new MethodClosure(Composers.class, "composeStringBody"));
    return map;
  }

  public static String composeJsonBody(Object body) {
    return JsonOutput.toJson(body);
  }

  public static String composeXmlBody(Object body) {
    String xmlData;
    if (body instanceof CharSequence) {
      xmlData = body.toString();
    } else if (body instanceof Node) {
      xmlData = XmlUtil.serialize((Node) body);
    } else {
      throw new IllegalArgumentException("body must be a String or a groovy.util.Node to be serialized to XML");
    }
    return xmlData;
  }

  public static String composeStringBody(Object body) {
    return String.valueOf(body);
  }

  public static byte[] composeBytesBody(Object body) throws IOException {
    byte[] bytes;
    if (body instanceof byte[]) {
      bytes = (byte[]) body;
    } else if (body instanceof Byte[]) {
      Byte[] bytes1 = (Byte[]) body;
      bytes = new byte[bytes1.length];
      for (int i = 0; i < bytes.length; i++) {
        bytes[i] = bytes1[i];
      }
    } else if (body instanceof InputStream) {
      bytes = IOGroovyMethods.getBytes((InputStream) body);
    } else {
      throw new IllegalArgumentException("Body must be a byte array or an InputStream to be serialized to bytes");
    }
    return bytes;
  }

  private static class MultipartFormComposerClosure extends Closure<RequestBody> {
    // keeping poet instance to have an up to date (and mostly non-null) composers
    private final HttpPoet poet;

    MultipartFormComposerClosure(HttpPoet poet) {
      super(null);
      this.poet = poet;
    }

    public RequestBody doCall(Object body) throws IOException {
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
          addFormDataPart(builder, key, (FormPart) value, poet.getComposers());
        } else {
          builder.addFormDataPart(key, String.valueOf(value));
        }
      }
      return builder.build();
    }

    private void addFormDataPart(MultipartBody.Builder builder, String key, File file) throws IOException {
      builder.addFormDataPart(key, file.getName(), RequestBody.create(ResourceGroovyMethods.getBytes(file), ContentType.BINARY.toMediaType()));
    }

    private void addFormDataPart(MultipartBody.Builder builder, String key, InputStream inputStream) throws IOException {
      builder.addFormDataPart(key, key, RequestBody.create(IOGroovyMethods.getBytes(inputStream), ContentType.BINARY.toMediaType()));
    }

    private void addFormDataPart(MultipartBody.Builder builder, String key, FormPart formPart, Map<ContentType, Closure<?>> composers) throws IOException {
      final Object value = formPart.getValue();
      MediaType mediaType = formPart.getContentType().toMediaType();
      String filename = formPart.getFilename() != null ? formPart.getFilename() : key;
      RequestBody requestBody;
      // smart conversions
      if (value instanceof File) {
        requestBody = RequestBody.create(ResourceGroovyMethods.getBytes((File) value), mediaType);;
      } else if (value instanceof Path) {
        requestBody = RequestBody.create(ResourceGroovyMethods.getBytes(((Path) value).toFile()), mediaType);;
      } else if (value instanceof Reader) {
        requestBody = RequestBody.create(IOGroovyMethods.getText((Reader) value), mediaType);
      } else {
        Closure<?> composer = composers.get(formPart.getContentType());
        if (composer == null) {
          throw new IllegalArgumentException(String.format("Couldn't find a composer for FormPart '%s'", filename));
        }
        requestBody = (RequestBody) composer.call(value);
      }
      builder.addFormDataPart(key, filename, requestBody);
    }
  }
}
