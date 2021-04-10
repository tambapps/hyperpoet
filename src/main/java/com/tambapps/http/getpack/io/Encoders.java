package com.tambapps.http.getpack.io;

import groovy.json.JsonOutput;
import groovy.lang.Closure;
import groovy.util.Node;
import groovy.xml.XmlUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.codehaus.groovy.runtime.MethodClosure;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Encoders {

  public static Map<MediaType, Closure<?>> getMap() {
    Map<MediaType, Closure<?>> map = new HashMap<>();
    Encoders encoders = new Encoders();
    map.put(MediaType.get("application/json"), new MethodClosure(encoders, "encodeJsonBody"));
    map.put(MediaType.get("application/xml"), new MethodClosure(encoders, "encodeXmlBody"));
    return map;
  }

  private Encoders() {}

  protected RequestBody encodeJsonBody(Object body, MediaType mediaType) {
    String jsonBody;
    if (body instanceof CharSequence) {
      jsonBody = body.toString();
    } else {
      jsonBody = JsonOutput.toJson(body);
    }
    return RequestBody.create(jsonBody.getBytes(StandardCharsets.UTF_8), mediaType);
  }

  protected RequestBody encodeXmlBody(Object body, MediaType mediaType) {
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

  /*
  protected RequestBody encodeBody(Object body, MediaType contentType) throws IOException {
    // this also handles MultipartBody
    if (body instanceof RequestBody) {
      return (RequestBody) body;
    }
    if (contentType == null) {
      return RequestBody.create(String.valueOf(body).getBytes(StandardCharsets.UTF_8));
    }
    switch (ContentType.HTML) {
      case JSON:
        String jsonBody;
        if (body instanceof CharSequence) {
          jsonBody = body.toString();
        } else {
          jsonBody = JsonOutput.toJson(body);
        }
        return RequestBody.create(jsonBody.getBytes(StandardCharsets.UTF_8), contentType.getMediaType());
      case XML:
        String xmlData;
        if (body instanceof CharSequence) {
          xmlData = body.toString();
        } else if (body instanceof Node) {
          xmlData = XmlUtil.serialize((Node) body);
        } else {
          throw new IllegalArgumentException("body must be a String or a groovy.util.Node to be serialized to XML");
        }
        return RequestBody.create(xmlData.getBytes(StandardCharsets.UTF_8), contentType.getMediaType());
      case TEXT:
      case HTML:
        return RequestBody.create(String.valueOf(body).getBytes(StandardCharsets.UTF_8), contentType.getMediaType());
      case BINARY:
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
          throw new IllegalArgumentException("body must be a byte array or an InputStream to be serialized to XML");
        }
        return RequestBody.create(bytes, contentType.getMediaType());
      default:
        throw new UnsupportedOperationException(contentType + " type is not handled");
    }
  }
   */
}
