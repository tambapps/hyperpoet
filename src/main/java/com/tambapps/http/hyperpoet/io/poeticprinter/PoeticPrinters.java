package com.tambapps.http.hyperpoet.io.poeticprinter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tambapps.http.hyperpoet.ContentType;
import com.tambapps.http.hyperpoet.Function;
import com.tambapps.http.hyperpoet.io.json.JsonGenerator;
import com.tambapps.http.hyperpoet.util.ContentTypeMapFunction;

public final class PoeticPrinters {

  private PoeticPrinters() {}

  public static ContentTypeMapFunction getMap(JsonGenerator generator) {
    ContentTypeMapFunction map = new ContentTypeMapFunction();
    map.put(ContentType.JSON, generator::composeToPrettyColoredJson);
    return map;
  }

}
