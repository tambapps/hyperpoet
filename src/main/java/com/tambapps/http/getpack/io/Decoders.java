package com.tambapps.http.getpack.io;

import com.tambapps.http.getpack.MediaTypes;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.util.XmlSlurper;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.codehaus.groovy.runtime.MethodClosure;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Decoders {

  private Decoders() {}

  public static Map<MediaType, Closure<?>> getMap() {
    Map<MediaType, Closure<?>> map = new HashMap<>();
    map.put(MediaTypes.JSON, new MethodClosure(Decoders.class, "decodeJsonResponseBody"));
    map.put(MediaTypes.XML, new MethodClosure(Decoders.class, "decodeXmlResponseBody"));
    map.put(MediaTypes.TEXT, new MethodClosure(Decoders.class, "decodeStringResponseBody"));
    map.put(MediaTypes.HTML, new MethodClosure(Decoders.class, "decodeStringResponseBody"));
    map.put(MediaTypes.BINARY, new MethodClosure(Decoders.class, "decodeBytesResponseBody"));
    // default decoder (when no content type was found)
    map.put(null, new MethodClosure(Decoders.class, "decodeStringResponseBody"));
    return map;
  }

  public static Object decodeJsonResponseBody(ResponseBody body) throws IOException {
    return new JsonSlurper().parseText(body.string());
  }

  public static Object decodeXmlResponseBody(ResponseBody body) throws IOException {
    try {
      return new XmlSlurper().parseText(body.string());
    } catch (SAXException | ParserConfigurationException e) {
      throw new IOException("An error occureed whilte attempting to load XML resopnse body");
    }
  }

  public static String decodeStringResponseBody(ResponseBody body) throws IOException {
    return body.string();
  }

  public static byte[] decodeBytesResponseBody(ResponseBody body) throws IOException {
    return body.bytes();
  }
}
