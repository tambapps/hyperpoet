package com.tambapps.http.getpack.io;

import com.tambapps.http.getpack.MediaTypes;
import groovy.json.JsonOutput;
import groovy.lang.Closure;
import groovy.util.Node;
import groovy.xml.XmlUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Encoders {

  private Encoders() {}

  public static Map<MediaType, Closure<?>> getMap() {
    Map<MediaType, Closure<?>> map = new HashMap<>();
    map.put(MediaTypes.JSON, new MethodClosure(Encoders.class, "encodeJsonBody"));
    map.put(MediaTypes.XML, new MethodClosure(Encoders.class, "encodeXmlBody"));
    map.put(MediaTypes.TEXT, new MethodClosure(Encoders.class, "encodeStringBody"));
    map.put(MediaTypes.HTML, new MethodClosure(Encoders.class, "encodeStringBody"));
    map.put(MediaTypes.BINARY, new MethodClosure(Encoders.class, "encodeBytesBody"));
    // null is default
    map.put(null, new MethodClosure(Encoders.class, "encodeStringBody"));
    return map;
  }

  public static RequestBody encodeJsonBody(Object body, MediaType mediaType) {
    String jsonBody;
    if (body instanceof CharSequence) {
      jsonBody = body.toString();
    } else {
      jsonBody = JsonOutput.toJson(body);
    }
    return RequestBody.create(jsonBody.getBytes(StandardCharsets.UTF_8), mediaType);
  }

  public static RequestBody encodeXmlBody(Object body, MediaType mediaType) {
    String xmlData;
    if (body instanceof CharSequence) {
      xmlData = body.toString();
    } else if (body instanceof Node) {
      xmlData = XmlUtil.serialize((Node) body);
    } else {
      throw new IllegalArgumentException("body must be a String or a groovy.util.Node to be serialized to XML");
    }
    return RequestBody.create(xmlData.getBytes(StandardCharsets.UTF_8), mediaType);
  }

  public static RequestBody encodeStringBody(Object body, MediaType mediaType) {
    return RequestBody.create(String.valueOf(body).getBytes(StandardCharsets.UTF_8), mediaType);
  }

  public static RequestBody encodeBytesBody(Object body, MediaType mediaType) throws IOException {
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
      throw new IllegalArgumentException("body must be a byte array or an InputStream to be serialized to bytes");
    }
    return RequestBody.create(bytes, mediaType);
  }

}
