package com.tambapps.http.hyperpoet;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

@Value(staticConstructor = "of")
public class JsonPatch {

  public static JsonPatch add(String path, Object value) {
    return of("add", path, null, value);
  }

  public static JsonPatch remove(String path) {
    return of("remove", path, null, null);
  }

  public static JsonPatch replace(String path, Object value) {
    return of("replace", path, null, value);
  }

  public static JsonPatch move(String from, String path) {
    return of("move", path, from, null);
  }

  public static JsonPatch test(String path, Object value) {
    return of("test", path, null, value);
  }

  String op;
  String path;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String from;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Object value;
}
