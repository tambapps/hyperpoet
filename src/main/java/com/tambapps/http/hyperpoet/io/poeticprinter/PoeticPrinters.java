package com.tambapps.http.hyperpoet.io.poeticprinter;

import com.tambapps.http.hyperpoet.ContentType;
import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

public final class PoeticPrinters {

  private PoeticPrinters() {}

  public static Map<ContentType, Closure<?>> getMap() {
    Map<ContentType, Closure<?>> map = new HashMap<>();
    JsonPoeticPrinter printer = new JsonPoeticPrinter();
    map.put(ContentType.JSON, printer);
    map.put(ContentType.PROBLEM_JSON, printer);
    return map;
  }

}
