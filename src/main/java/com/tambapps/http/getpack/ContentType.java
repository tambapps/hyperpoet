package com.tambapps.http.getpack;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Value
public class ContentType {

  String contentType;
  @EqualsAndHashCode.Exclude
  List<String> additionalParameters;

  public static ContentType from(String headerValue) {
    String[] fields = headerValue.split(";");
    List<String> additionalParameters = new ArrayList<>(fields.length - 1);
    for (int i = 1; i < fields.length; i++) {
      additionalParameters.add(fields[i].trim());
    }
    return new ContentType(fields[0].trim(), additionalParameters);
  }

  @Override
  public String toString() {
    return contentType + ";" + String.join("; ", additionalParameters);
  }
}
