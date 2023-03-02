package com.tambapps.http.hyperpoet.io.parser;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.Function;
import com.tambapps.http.hyperpoet.util.ContentTypeMapFunction;
import groovy.xml.XmlSlurper;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Utility class holding several common parsers. A parser can return any kind of objects
 */
public class Parsers {

  private Parsers() {}

  public static ContentTypeMapFunction getMap() {
    ContentTypeMapFunction map = new ContentTypeMapFunction();
    map.put(ContentType.JSON, new JsonParserClosure());
    map.put(ContentType.XML, (o) -> parseXmlResponseBody((ResponseBody) o));
    map.put(ContentType.TEXT, (o) -> parseStringResponseBody((ResponseBody) o));
    map.put(ContentType.HTML, (o) -> parseStringResponseBody((ResponseBody) o));
    map.put(ContentType.BINARY, (o) -> parseBytesResponseBody((ResponseBody) o));
    // default parser (when no content type was found)
    map.setDefaultValue((o) -> parseStringResponseBody((ResponseBody) o));
    return map;
  }

  @SneakyThrows
  public static Object parseXmlResponseBody(ResponseBody body) {
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

  @SneakyThrows
  public static byte[] parseBytesResponseBody(ResponseBody body) {
    return body.bytes();
  }
}
