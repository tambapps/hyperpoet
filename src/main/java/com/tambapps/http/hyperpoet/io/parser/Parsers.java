package com.tambapps.http.hyperpoet.io.parser;

import com.tambapps.http.hyperpoet.ContentType;
import groovy.lang.Closure;
import groovy.xml.XmlSlurper;
import lombok.SneakyThrows;
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
    JsonParserClosure jsonParserClosure = new JsonParserClosure();
    map.put(ContentType.JSON, jsonParserClosure);
    map.put(ContentType.PROBLEM_JSON, jsonParserClosure);
    map.put(ContentType.XML, new MethodClosure(Parsers.class, "parseXmlResponseBody"));
    map.put(ContentType.TEXT, new MethodClosure(Parsers.class, "parseStringResponseBody"));
    map.put(ContentType.HTML, new MethodClosure(Parsers.class, "parseStringResponseBody"));
    map.put(ContentType.BINARY, new MethodClosure(Parsers.class, "parseBytesResponseBody"));
    // default parser (when no content type was found)
    map.put(null, new MethodClosure(Parsers.class, "parseStringResponseBody"));
    return map;
  }

  public static Object parseXmlResponseBody(ResponseBody body) throws IOException {
    String text = body.string();
    if (text.isEmpty()) {
      return "(No content)";
    }
    try {
      return new XmlSlurper().parseText(text);
    } catch (SAXException | ParserConfigurationException e) {
      throw new IOException("An error occurred while attempting to load XML response body");
    }
  }

  @SneakyThrows
  public static String parseStringResponseBody(ResponseBody body) {
    String text = body.string();
    if (text.isEmpty()) {
      return "(No content)";
    }
    return text;
  }

  public static byte[] parseBytesResponseBody(ResponseBody body) throws IOException {
    return body.bytes();
  }
}
