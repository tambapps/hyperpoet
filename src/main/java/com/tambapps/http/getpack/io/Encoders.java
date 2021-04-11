package com.tambapps.http.getpack.io;

import com.tambapps.http.getpack.ContentType;
import groovy.json.JsonOutput;
import groovy.lang.Closure;
import groovy.util.Node;
import groovy.xml.XmlUtil;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class holding several common encoders.
 * An encoder should return one of the following types
 * - a byte array (primitive byte, not Byte)
 * - an InputStream
 * - a String
 *
 */
public final class Encoders {

  private Encoders() {}

  public static Map<ContentType, Closure<?>> getMap() {
    Map<ContentType, Closure<?>> map = new HashMap<>();
    map.put(ContentType.JSON, new MethodClosure(Encoders.class, "encodeJsonBody"));
    map.put(ContentType.XML, new MethodClosure(Encoders.class, "encodeXmlBody"));
    map.put(ContentType.TEXT, new MethodClosure(Encoders.class, "encodeStringBody"));
    map.put(ContentType.HTML, new MethodClosure(Encoders.class, "encodeStringBody"));
    map.put(ContentType.BINARY, new MethodClosure(Encoders.class, "encodeBytesBody"));
    // default encoder (when no content type was found)
    map.put(null, new MethodClosure(Encoders.class, "encodeStringBody"));
    return map;
  }

  public static String encodeJsonBody(Object body) {
    String jsonBody;
    if (body instanceof CharSequence) {
      jsonBody = body.toString();
    } else {
      jsonBody = JsonOutput.toJson(body);
    }
    return jsonBody;
  }

  public static String encodeXmlBody(Object body) {
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

  public static String encodeStringBody(Object body) {
    return String.valueOf(body);
  }

  public static byte[] encodeBytesBody(Object body) throws IOException {
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

}
