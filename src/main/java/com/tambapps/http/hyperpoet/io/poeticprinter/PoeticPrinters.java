package com.tambapps.http.hyperpoet.io.poeticprinter;

import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.Function;
import com.tambapps.http.hyperpoet.util.ContentTypeMapFunction;

public final class PoeticPrinters {

  private PoeticPrinters() {}

  public static ContentTypeMapFunction getMap() {
    ContentTypeMapFunction map = new ContentTypeMapFunction();
    map.put(ContentType.JSON, new JsonPoeticPrinter());
    return map;
  }

}
