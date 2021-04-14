package com.tambapps.http.hyperpoet.io;

import com.tambapps.http.hyperpoet.ContentType;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.util.XmlSlurper;
import okhttp3.ResponseBody;
import org.codehaus.groovy.runtime.MethodClosure;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class holding several common parsers. A parser can return any kind of objects
 */
public class Parsers {

  private Parsers() {}

  public static Map<ContentType, Closure<?>> getMap() {
    Map<ContentType, Closure<?>> map = new HashMap<>();
    map.put(ContentType.JSON, new MethodClosure(Parsers.class, "parseJsonResponseBody"));
    map.put(ContentType.XML, new MethodClosure(Parsers.class, "parseXmlResponseBody"));
    map.put(ContentType.TEXT, new MethodClosure(Parsers.class, "parseStringResponseBody"));
    map.put(ContentType.HTML, new MethodClosure(Parsers.class, "parseStringResponseBody"));
    map.put(ContentType.BINARY, new MethodClosure(Parsers.class, "parseBytesResponseBody"));
    // default parser (when no content type was found)
    map.put(null, new MethodClosure(Parsers.class, "parseStringResponseBody"));
    return map;
  }

  public static Object parseJsonResponseBody(ResponseBody body) throws IOException {
    return new JsonSlurper().parseText(body.string());
  }

  public static Object parseXmlResponseBody(ResponseBody body) throws IOException {
    try {
      return new XmlSlurper().parseText(body.string());
    } catch (SAXException | ParserConfigurationException e) {
      throw new IOException("An error occurred while attempting to load XML response body");
    }
  }

  public static String parseStringResponseBody(ResponseBody body) throws IOException {
    return body.string();
  }

  public static byte[] parseBytesResponseBody(ResponseBody body) throws IOException {
    return body.bytes();
  }
}
