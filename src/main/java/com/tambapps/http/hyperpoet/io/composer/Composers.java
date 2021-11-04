package com.tambapps.http.hyperpoet.io.composer;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.util.QueryParamComposer;
import groovy.json.JsonGenerator;
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

  public static Map<ContentType, Closure<?>> getMap(JsonGenerator jsonGenerator, QueryParamComposer queryParamComposer) {
    Map<ContentType, Closure<?>> map = new HashMap<>();
    map.put(ContentType.JSON, new MethodClosure(jsonGenerator, "toJson"));
    map.put(ContentType.XML, new MethodClosure(Composers.class, "composeXmlBody"));
    map.put(ContentType.TEXT, new MethodClosure(Composers.class, "composeStringBody"));
    map.put(ContentType.HTML, new MethodClosure(Composers.class, "composeStringBody"));
    map.put(ContentType.BINARY, new MethodClosure(Composers.class, "composeBytesBody"));
    map.put(ContentType.URL_ENCODED, new MethodClosure(queryParamComposer, "compose"));
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

}
