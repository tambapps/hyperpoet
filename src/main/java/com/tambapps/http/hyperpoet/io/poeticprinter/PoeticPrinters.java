package com.tambapps.http.hyperpoet.io.poeticprinter;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.Function;
import com.tambapps.http.hyperpoet.util.ContentTypeMap;

public final class PoeticPrinters {

  private PoeticPrinters() {}

  public static ContentTypeMap<Function> getMap() {
    ContentTypeMap<Function> map = new ContentTypeMap<>();
    map.put(ContentType.JSON, new JsonPoeticPrinter());
    return map;
  }

}
