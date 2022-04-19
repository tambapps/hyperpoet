package com.tambapps.http.hyperpoet.io.poeticprinter;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.util.ContentTypeMap;
import groovy.lang.Closure;

public final class PoeticPrinters {

  private PoeticPrinters() {}

  public static ContentTypeMap< Closure<?>> getMap() {
    ContentTypeMap<Closure<?>> map = new ContentTypeMap<>();
    map.put(ContentType.JSON, new JsonPoeticPrinter());
    return map;
  }

}
