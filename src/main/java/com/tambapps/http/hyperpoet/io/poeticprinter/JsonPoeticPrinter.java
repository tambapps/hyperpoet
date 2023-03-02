package com.tambapps.http.hyperpoet.io.poeticprinter;

import static com.tambapps.http.hyperpoet.util.Ansi.println;

import com.tambapps.http.hyperpoet.io.json.JsonGenerator;
import groovy.json.StringEscapeUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.Map;

@AllArgsConstructor
public class JsonPoeticPrinter extends AbstractPoeticPrinter {

  private final JsonGenerator jsonGenerator;

  @SneakyThrows
  @Override
  public void printBytes(byte[] bytes) {
    Map object = jsonGenerator.getMapper().readValue(bytes, Map.class);
    println(StringEscapeUtils.unescapeJavaScript(jsonGenerator.composeToPrettyColoredJson(object)));
  }

}
