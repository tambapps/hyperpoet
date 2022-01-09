package com.tambapps.http.hyperpoet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import okhttp3.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a content type. It only consider the actual content type from header for
 * equality/hashCode.
 * E.g for the value 'application/json; charset=UTF-8' it will only consider 'application/json'
 * for equality/hashCode
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Value
public class ContentType {

  public static final ContentType JSON = new ContentType("application/json");
  public static final ContentType XML = new ContentType("application/xml");
  public static final ContentType TEXT = new ContentType("text/plain");
  public static final ContentType HTML = new ContentType("text/html");
  public static final ContentType BINARY = new ContentType("application/octet-stream");
  public static final ContentType URL_ENCODED = new ContentType("application/x-www-form-urlencoded");

  String contentType;
  @EqualsAndHashCode.Exclude
  List<String> additionalParameters;

  private ContentType(String contentType) {
    this(contentType, Collections.emptyList());
  }

  public static ContentType from(String headerValue) {
    String[] fields = headerValue.split(";");
    List<String> additionalParameters = new ArrayList<>(fields.length - 1);
    for (int i = 1; i < fields.length; i++) {
      additionalParameters.add(fields[i].trim());
    }
    return new ContentType(fields[0].trim(), additionalParameters);
  }

  public MediaType toMediaType() {
    return MediaType.get(toString());
  }
  @Override
  public String toString() {
    if (additionalParameters.isEmpty()) {
      return contentType;
    }
    return contentType + ";" + String.join("; ", additionalParameters);
  }
}
